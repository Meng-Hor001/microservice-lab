package kh.edu.istad.platform.infrastructure.authorization;

import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationValidator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AuthorizationServerSecurityConfig {
    @Bean
    @Order(0)
    SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            com.nimbusds.jose.jwk.source.JWKSource<
                    com.nimbusds.jose.proc.SecurityContext> jwkSource
    ) throws Exception {
        http.oauth2AuthorizationServer(authorizationServer -> {
            http.securityMatcher(authorizationServer.getEndpointsMatcher());
            authorizationServer.authorizationEndpoint(endpoint -> endpoint.consentPage("/oauth2/consent"));

            authorizationServer.oidc(oidc ->
                    oidc.clientRegistrationEndpoint(endpoint ->
                            endpoint.authenticationProviders(providers ->
                                    providers.forEach(provider -> {
                                        if (provider instanceof OidcClientRegistrationAuthenticationProvider registration) {
                                            registration.setAuthenticationValidator(
                                                    OidcClientRegistrationAuthenticationValidator.DEFAULT_REDIRECT_URI_VALIDATOR
                                                            .andThen(OidcClientRegistrationAuthenticationValidator.DEFAULT_POST_LOGOUT_REDIRECT_URI_VALIDATOR)
                                                            .andThen(OidcClientRegistrationAuthenticationValidator.DEFAULT_JWK_SET_URI_VALIDATOR)
                                                            .andThen(context -> {
                                                                var scopes = context.<org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcClientRegistrationAuthenticationToken>getAuthentication()
                                                                        .getClientRegistration().getScopes();
                                                                var allowed = Set.of("openid", "profile", "email", "phone", "address");
                                                                if (scopes != null && !allowed.containsAll(scopes)) {
                                                                    throw new OAuth2AuthenticationException(
                                                                            new OAuth2Error("invalid_scope", "Unsupported registration scope", null));
                                                                }
                                                            }));
                                        }
                                    }))));
        });

        http.authorizeHttpRequests(authorize ->
                authorize.anyRequest().authenticated()
        );

        http.oauth2ResourceServer(resourceServer ->
                resourceServer.jwt(jwt ->
                        jwt.decoder(
                                org.springframework.security.config.annotation.web.configuration
                                        .OAuth2AuthorizationServerConfiguration
                                        .jwtDecoder(jwkSource)
                        )
                )
        );

        http.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login"),
                new org.springframework.security.web.util.matcher.MediaTypeRequestMatcher(org.springframework.http.MediaType.TEXT_HTML)));
        return http.build();
    }
}
