package kh.edu.istad.platform.infrastructure.authorization;

import kh.edu.istad.platform.iam.application.usecase.GetUserAuthoritiesUseCase;
import kh.edu.istad.platform.iam.application.usecase.LoadUserForAuthenticationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServerTokenConfig {
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(
            LoadUserForAuthenticationUseCase loadUser,
            GetUserAuthoritiesUseCase getUserAuthorities,
            @Value("${spring.security.oauth2.resourceserver.jwt.audiences[0]}") String apiAudience
    ) {
        return context -> {
            if (!(context.getPrincipal().getPrincipal()
                    instanceof UserDetails principal)) {
                return;
            }

            var user = loadUser.execute(principal.getUsername());
            context.getClaims().subject(user.uuid().toString());

            if (OidcParameterNames.ID_TOKEN.equals(
                    context.getTokenType().getValue())) {

                var scopes = context.getAuthorizedScopes();

                if (scopes.contains(OidcScopes.PROFILE)) {
                    String name = Stream.of(
                                    user.givenName(),
                                    user.familyName()
                            )
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining(" "));

                    claim(context, "name", name);
                    claim(context, "given_name", user.givenName());
                    claim(context, "family_name", user.familyName());
                    claim(context, "middle_name", user.middleName());
                    claim(context, "nickname", user.nickname());
                    claim(context, "preferred_username", user.username());
                    claim(context, "profile", user.profileUrl());
                    claim(context, "picture", user.pictureUrl());
                    claim(context, "website", user.websiteUrl());
                    claim(context, "gender", user.gender());

                    claim(context, "birthdate",
                            user.birthdate() == null
                                    ? null
                                    : user.birthdate().toString());

                    claim(context, "zoneinfo", user.zoneinfo());
                    claim(context, "locale", user.locale());

                    claim(context, "updated_at",
                            user.updatedAt() == null
                                    ? null
                                    : user.updatedAt().getEpochSecond());
                }

                if (scopes.contains(OidcScopes.EMAIL)) {
                    claim(context, "email", user.email());
                    claim(context, "email_verified", user.emailVerified());
                }

                if (scopes.contains(OidcScopes.PHONE)) {
                    claim(context, "phone_number", user.phoneNumber());
                    claim(context, "phone_number_verified",
                            user.phoneNumberVerified());
                }

                if (scopes.contains(OidcScopes.ADDRESS)) {
                    var address = new LinkedHashMap<String, Object>();

                    address.put("street_address",
                            user.addressStreetAddress());
                    address.put("locality", user.addressLocality());
                    address.put("region", user.addressRegion());
                    address.put("postal_code", user.addressPostalCode());
                    address.put("country", user.addressCountry());

                    address.values().removeIf(Objects::isNull);

                    if (!address.isEmpty()) {
                        claim(context, "address", address);
                    }
                }
            }

            if (OAuth2TokenType.ACCESS_TOKEN.equals(
                    context.getTokenType())) {
                context.getClaims().audience(new ArrayList<>(List.of(apiAudience)));

                var authorities =
                        getUserAuthorities.execute(user.uuid());

                claim(context, "roles",
                        new ArrayList<>(authorities.roles().stream()
                                .map(role -> role.name())
                                .toList()));

                claim(context, "permissions",
                        new ArrayList<>(authorities.effectivePermissions()));
            }
        };
    }

    private static void claim(
            JwtEncodingContext context,
            String name,
            Object value
    ) {
        if (value != null) {
            context.getClaims().claim(name, value);
        }
    }
}
