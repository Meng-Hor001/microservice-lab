package kh.edu.istad.platform.infrastructure.security;

import kh.edu.istad.platform.iam.application.dto.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.application.usecase.FindOrProvisionFederatedUserUseCase;
import kh.edu.istad.platform.iam.application.usecase.GetUserAuthoritiesUseCase;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service("federatedOidcUserService")
@RequiredArgsConstructor
public class FederatedOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();

    private final FindOrProvisionFederatedUserUseCase federation;
    private final GetUserAuthoritiesUseCase getAuthorities;
    private final FederatedAccountLinker accountLinker;

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {

        OidcUser providerUser = delegate.loadUser(request);

        String registrationId =
                request.getClientRegistration().getRegistrationId();

        boolean telegram = "telegram".equals(registrationId);
        if (!"google".equals(registrationId) && !telegram) {
            throw failure("This OIDC provider is not configured yet");
        }

        String email = providerUser.getEmail();

        if (!accountLinker.pending(registrationId) && !telegram && (email == null
                || email.isBlank()
                || !Boolean.TRUE.equals(providerUser.getEmailVerified()))) {
            throw failure("Google must supply a verified email");
        }

        var command = new FederatedIdentityCommand(
                telegram ? IdentityProvider.TELEGRAM : IdentityProvider.GOOGLE,
                providerUser.getSubject(),
                email,
                !telegram && Boolean.TRUE.equals(providerUser.getEmailVerified()),
                providerUser.getFullName(),
                telegram ? providerUser.getPreferredUsername() : null,
                providerUser.getPicture(),
                jsonAttributes(providerUser.getClaims())
        );

        kh.edu.istad.platform.iam.application.dto.AuthUserResult user;

        try {
            user = accountLinker.linkOrProvision(command, () -> federation.execute(command));
        } catch (IllegalArgumentException exception) {
            if (telegram) {
                throw failure("Telegram identity is not linked. Sign in to your IAM account "
                        + "and link Telegram; new users require verified email onboarding.");
            }
            throw failure(
                    "Unable to provision this account; sign in locally "
                            + "and link the provider instead"
            );
        }

        if (!user.enabled()
                || !user.accountNonExpired()
                || !user.accountNonLocked()
                || !user.credentialsNonExpired()) {
            throw failure("The local IAM account is unavailable");
        }

        var localAuthorities = getAuthorities.execute(user.uuid());

        var authorities = new ArrayList<GrantedAuthority>(
                providerUser.getAuthorities()
        );

        localAuthorities.roles().forEach(role ->
                authorities.add(
                        new SimpleGrantedAuthority("ROLE_" + role.name())
                )
        );

        localAuthorities.effectivePermissions().forEach(permission ->
                authorities.add(
                        new SimpleGrantedAuthority(permission)
                )
        );

        return new IamOidcUser(user, providerUser, authorities);
    }

    static java.util.Map<String, Object> jsonAttributes(java.util.Map<String, Object> claims) {
        var result = new java.util.LinkedHashMap<String, Object>();
        claims.forEach((key, value) -> result.put(key, jsonValue(value)));
        return result;
    }

    private static Object jsonValue(Object value) {
        if (value == null || value instanceof String
                || value instanceof Boolean || value instanceof Number) {
            return value;
        }
        if (value instanceof java.net.URL || value instanceof java.net.URI
                || value instanceof java.time.temporal.TemporalAccessor) {
            return value.toString();
        }
        if (value instanceof java.util.Map<?, ?> map) {
            var result = new java.util.LinkedHashMap<String, Object>();
            map.forEach((key, item) -> {
                if (!(key instanceof String text)) {
                    throw new IllegalArgumentException("Claim map keys must be strings");
                }
                result.put(text, jsonValue(item));
            });
            return result;
        }
        if (value instanceof java.util.Collection<?> collection) {
            return collection.stream().map(FederatedOidcUserService::jsonValue).toList();
        }
        throw new IllegalArgumentException("Unsupported claim type: " + value.getClass().getName());
    }

    private static OAuth2AuthenticationException failure(String message) {
        return new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user_info", message, null)
        );
    }
}
