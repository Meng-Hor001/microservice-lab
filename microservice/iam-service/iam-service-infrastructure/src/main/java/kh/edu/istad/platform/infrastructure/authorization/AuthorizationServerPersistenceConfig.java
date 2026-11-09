package kh.edu.istad.platform.infrastructure.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServerPersistenceConfig {
    @Bean
    RegisteredClientRepository registeredClientRepository(
            JdbcOperations jdbcOperations
    ) {
        return new JdbcRegisteredClientRepository(jdbcOperations);
    }

    @Bean
    OAuth2AuthorizationService authorizationService(
            JdbcOperations jdbcOperations,
            RegisteredClientRepository clients
    ) {
        return new JdbcOAuth2AuthorizationService(jdbcOperations, clients);
    }

    @Bean
    OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcOperations jdbcOperations,
            RegisteredClientRepository clients
    ) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, clients);
    }
}
