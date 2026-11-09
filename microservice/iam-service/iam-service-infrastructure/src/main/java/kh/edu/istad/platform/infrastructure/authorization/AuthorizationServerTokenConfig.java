package kh.edu.istad.platform.infrastructure.authorization;

import kh.edu.istad.platform.iam.application.usecase.GetUserAuthoritiesUseCase;
import kh.edu.istad.platform.iam.application.usecase.LoadUserForAuthenticationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServerTokenConfig {
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(
            LoadUserForAuthenticationUseCase loadUser,
            GetUserAuthoritiesUseCase getUserAuthorities
    ) {
        return context -> {
            if (!(context.getPrincipal().getPrincipal()
                    instanceof UserDetails userDetails)) {
                return;
            }

            var user = loadUser.execute(userDetails.getUsername());
            context.getClaims().subject(user.uuid().toString());

            if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                var scopes = context.getAuthorizedScopes();

                if (scopes.contains(OidcScopes.PROFILE)) {
                    if (user.givenName() != null) {
                        context.getClaims().claim("given_name", user.givenName());
                    }
                    if (user.familyName() != null) {
                        context.getClaims().claim("family_name", user.familyName());
                    }
                    if (user.username() != null) {
                        context.getClaims().claim("preferred_username", user.username());
                    }
                    if (user.pictureUrl() != null) {
                        context.getClaims().claim("picture", user.pictureUrl());
                    }
                }

                if (scopes.contains(OidcScopes.EMAIL)) {
                    if (user.email() != null) {
                        context.getClaims().claim("email", user.email());
                    }
                    context.getClaims().claim("email_verified", user.emailVerified());
                }
            }

            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                var authorities = getUserAuthorities.execute(user.uuid());

                context.getClaims().claim(
                        "roles",
                        authorities.roles().stream()
                                .map(role -> role.name())
                                .toList()
                );

                context.getClaims().claim(
                        "permissions",
                        authorities.effectivePermissions()
                );
            }
        };
    }
}
