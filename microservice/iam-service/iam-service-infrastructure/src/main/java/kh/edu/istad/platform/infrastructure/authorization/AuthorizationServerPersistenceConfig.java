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
import org.springframework.security.jackson.SecurityJacksonModules;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

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
        var mapper = JsonMapper.builder().addModules(SecurityJacksonModules.getModules(
                getClass().getClassLoader(), BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Long.class))).build();
        var service = new JdbcOAuth2AuthorizationService(jdbcOperations, clients);
        service.setAuthorizationRowMapper(
                new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(clients, mapper));
        service.setAuthorizationParametersMapper(
                new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationParametersMapper(mapper));
        return service;
    }

    @Bean
    OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcOperations jdbcOperations,
            RegisteredClientRepository clients
    ) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, clients);
    }
}
