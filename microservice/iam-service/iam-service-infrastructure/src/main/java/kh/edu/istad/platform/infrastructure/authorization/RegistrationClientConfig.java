package kh.edu.istad.platform.infrastructure.authorization;

import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Configuration(proxyBeanMethods = false)
public class RegistrationClientConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    ApplicationRunner seedWebClient(
            RegisteredClientRepository clients,
            @Value("${iam.web-client.id:iam-web}") String clientId,
            @Value("${iam.web-client.redirect-uri:http://localhost:5173/me/callback,http://localhost:9999/me/callback}") List<String> redirectUris,
            @Value("${iam.web-client.post-logout-redirect-uri:http://localhost:5173/me/,http://localhost:9999/me/}") List<String> postLogoutUris
    ) {
        return args -> {
            RegisteredClient existing = clients.findByClientId(clientId);
            if (existing != null) {
                // Keep the stored redirect URIs in step with config, e.g. after the dashboard moved paths.
                if (!existing.getRedirectUris().equals(Set.copyOf(redirectUris))
                        || !existing.getPostLogoutRedirectUris().equals(Set.copyOf(postLogoutUris))) {
                    clients.save(RegisteredClient.from(existing)
                            .redirectUris(uris -> {
                                uris.clear();
                                uris.addAll(redirectUris);
                            })
                            .postLogoutRedirectUris(uris -> {
                                uris.clear();
                                uris.addAll(postLogoutUris);
                            })
                            .build());
                }
                return;
            }

            RegisteredClient client = RegisteredClient
                    .withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientName("IAM account dashboard")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUris(uris -> uris.addAll(redirectUris))
                    .postLogoutRedirectUris(uris -> uris.addAll(postLogoutUris))
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope(OidcScopes.EMAIL)
                    .scope(OidcScopes.PHONE)
                    .clientSettings(ClientSettings.builder()
                            .requireProofKey(true)
                            .requireAuthorizationConsent(false)
                            .build())
                    .build();

            clients.save(client);
        };
    }

    @Bean
    ApplicationRunner seedRegistrationClient(
            RegisteredClientRepository clients,
            PasswordEncoder passwordEncoder,
            @Value("${iam.registration-client.id}") String clientId,
            @Value("${iam.registration-client.secret}") String secret
    ) {
        return args -> {
            if (clients.findByClientId(clientId) != null) {
                return;
            }

            RegisteredClient client = RegisteredClient
                    .withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientSecret(passwordEncoder.encode(secret))
                    .clientAuthenticationMethod(
                            ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                    )
                    .authorizationGrantType(
                            AuthorizationGrantType.CLIENT_CREDENTIALS
                    )
                    .scope("client.create")
                    .build();

            clients.save(client);
        };
    }
}
