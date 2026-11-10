package kh.edu.istad.platform.infrastructure.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.UUID;

@Configuration(proxyBeanMethods = false)
public class RegistrationClientConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
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
