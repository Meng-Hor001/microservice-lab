package kh.edu.istad.platform.infrastructure.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServerSecurityConfig {
    @Bean
    @Order(0)
    SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http.oauth2AuthorizationServer(authorizationServer -> {
            http.securityMatcher(authorizationServer.getEndpointsMatcher());
            authorizationServer.oidc(Customizer.withDefaults());
        });

        http.authorizeHttpRequests(authorize ->
                authorize.anyRequest().authenticated()
        );

        return http.build();
    }
}
