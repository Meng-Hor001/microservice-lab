package kh.edu.istad.platform.infrastructure.security;

import kh.edu.istad.platform.iam.application.dto.FederatedIdentityCommand;
import kh.edu.istad.platform.iam.application.usecase.FindOrProvisionFederatedUserUseCase;
import kh.edu.istad.platform.iam.application.usecase.GetUserAuthoritiesUseCase;
import kh.edu.istad.platform.iam.domain.enums.IdentityProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("federatedOAuth2UserService")
public class FederatedOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();

    private final RestClient github = RestClient.builder()
            .baseUrl("https://api.github.com")
            .build();

    private final FindOrProvisionFederatedUserUseCase federation;
    private final GetUserAuthoritiesUseCase getAuthorities;
    private final FederatedAccountLinker accountLinker;

    public FederatedOAuth2UserService(
            FindOrProvisionFederatedUserUseCase federation,
            GetUserAuthoritiesUseCase getAuthorities,
            FederatedAccountLinker accountLinker
    ) {
        this.federation = federation;
        this.getAuthorities = getAuthorities;
        this.accountLinker = accountLinker;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        String provider =
                request.getClientRegistration().getRegistrationId();

        if (!"github".equals(provider) && !"facebook".equals(provider)) {
            throw failure("This provider is not configured yet");
        }

        OAuth2User providerUser = delegate.loadUser(request);

        boolean isGithub = "github".equals(provider);
        String email = isGithub
                ? verifiedEmail(request.getAccessToken().getTokenValue())
                : providerUser.getAttribute("email");
        if (!accountLinker.pending(provider) && (email == null || email.isBlank())) {
            throw failure("An email is required. Allow email access or use another account.");
        }

        Object id = providerUser.getAttribute("id");

        if (id == null) {
            throw failure("The provider did not return a user ID");
        }

        var command = new FederatedIdentityCommand(
                isGithub ? IdentityProvider.GITHUB : IdentityProvider.FACEBOOK,
                id.toString(),
                email,
                isGithub,
                providerUser.getAttribute("name"),
                isGithub ? providerUser.getAttribute("login") : null,
                isGithub ? providerUser.getAttribute("avatar_url") : facebookPicture(providerUser),
                FederatedOidcUserService.jsonAttributes(
                        providerUser.getAttributes()
                )
        );

        kh.edu.istad.platform.iam.application.dto.AuthUserResult user;

        try {
            user = accountLinker.linkOrProvision(command, () -> federation.execute(command));
        } catch (IllegalArgumentException exception) {
            throw failure(
                    "Cannot provision this account. If the email already "
                            + "belongs to a local account, sign in locally "
                            + "and link the provider instead."
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

        return new IamOAuth2User(
                user,
                authorities,
                providerUser.getAttributes()
        );
    }

    private static String facebookPicture(OAuth2User user) {
        Object picture = user.getAttribute("picture");
        if (picture instanceof Map<?, ?> pictureMap
                && pictureMap.get("data") instanceof Map<?, ?> data
                && data.get("url") instanceof String url) {
            return url;
        }
        return null;
    }

    private String verifiedEmail(String accessToken) {
        List<Map<String, Object>> emails;

        try {
            emails = github.get()
                    .uri("/user/emails")
                    .headers(headers -> {
                        headers.setBearerAuth(accessToken);
                        headers.set("Accept", "application/vnd.github+json");
                    })
                    .retrieve()
                    .body(new ParameterizedTypeReference<
                            List<Map<String, Object>>>() {
                    });
        } catch (RestClientException exception) {
            throw failure("Unable to fetch GitHub email addresses");
        }

        if (emails == null) {
            throw failure("GitHub returned no email addresses");
        }
        return emails.stream()
                .filter(item ->
                        Boolean.TRUE.equals(item.get("verified")))
                .sorted(java.util.Comparator.comparing(
                        item -> !Boolean.TRUE.equals(item.get("primary"))
                ))
                .map(item -> item.get("email"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(value -> !value.isBlank())
                .findFirst()
                .orElseThrow(() ->
                        failure("GitHub requires a verified email"));
    }

    private static OAuth2AuthenticationException failure(String message) {
        return new OAuth2AuthenticationException(
                new OAuth2Error("invalid_user_info", message, null)
        );
    }
}
