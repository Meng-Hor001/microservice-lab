package kh.edu.istad.platform.restapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class ClientRegistrationManagementSecurityConfig {
    @Bean
    @Order(1)
    SecurityFilterChain clientRegistrationManagementFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .securityMatcher("/connect/register/*")
                .csrf(csrf ->
                        csrf.ignoringRequestMatchers("/connect/register/*")
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().permitAll()
                );

        return http.build();
    }
}
