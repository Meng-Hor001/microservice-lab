package kh.edu.istad.platform.main.config;

import kh.edu.istad.platform.iam.application.port.out.OAuthClientRepository;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.application.usecase.ManageOAuthClientsUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration(proxyBeanMethods = false)
public class OAuthClientUseCaseConfig {
    @Bean
    public ManageOAuthClientsUseCase manageOAuthClientsUseCase(
            OAuthClientRepository clients,
            PasswordHasher hasher,
            @Value("${iam.web-client.id:iam-web}") String webClientId,
            @Value("${iam.registration-client.id}") String registrationClientId
    ) {
        // The dashboard client and the registration client are required by the platform itself.
        return new ManageOAuthClientsUseCase(clients, hasher, Set.of(webClientId, registrationClientId));
    }
}
