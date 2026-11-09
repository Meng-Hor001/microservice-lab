package kh.edu.istad.platform.restapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ApiSecurityConfig {
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            ApiSecurityProblemHandler problemHandler
    ) throws Exception {
        http
                .securityMatcher("/api/**", "/connect/register")
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/connect/register"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(problemHandler)
                        .accessDeniedHandler(problemHandler)
                )
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .authenticationEntryPoint(problemHandler)
                        .accessDeniedHandler(problemHandler)
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var permissions = new JwtGrantedAuthoritiesConverter();
        permissions.setAuthoritiesClaimName("permissions");
        permissions.setAuthorityPrefix("");

        var roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");

        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                new DelegatingJwtGrantedAuthoritiesConverter(
                        permissions,
                        roles
                )
        );

        return converter;
    }

    @Bean
    @Order(3)
    public SecurityFilterChain fallbackSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults());

        return http.build();
    }
}
