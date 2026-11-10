package kh.edu.istad.platform.restapi.security;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.usecase.RecordLoginAttemptUseCase;
import kh.edu.istad.platform.iam.application.usecase.RecordIdentityLoginUseCase;
import kh.edu.istad.platform.iam.application.usecase.LoadUserForAuthenticationUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

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
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
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
            HttpSecurity http,
            @Qualifier("federatedOidcUserService")
            OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService,
            @Qualifier("federatedOAuth2UserService")
            OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService,
            RecordLoginAttemptUseCase recordLoginAttempt,
            RecordIdentityLoginUseCase recordIdentityLogin,
            LoadUserForAuthenticationUseCase loadUser
    ) throws Exception {
        var successHandler = new LoginAuditSuccessHandler(
                recordLoginAttempt, recordIdentityLogin, loadUser);

        var failureRedirect = new SimpleUrlAuthenticationFailureHandler("/login?error");
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/login", "/register", "/iam.css",
                                "/error",
                                "/oauth2/authorization/**",
                                "/login/oauth2/code/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .failureHandler((request, response, exception) -> {
                            auditLogin(
                                    recordLoginAttempt,
                                    request.getParameter("username"),
                                    false
                            );

                            failureRedirect.onAuthenticationFailure(
                                    request, response, exception
                            );
                        })
                        .permitAll()
                )
                .oauth2Login(login -> login.loginPage("/login")
                        .successHandler(successHandler)
                        .failureUrl("/login?error")
                        .failureHandler((request, response, exception) -> {
                            var logger = org.slf4j.LoggerFactory.getLogger(ApiSecurityConfig.class);
                            if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauth) {
                                String description = "invalid_user_info".equals(oauth.getError().getErrorCode())
                                        ? oauth.getError().getDescription() : "See provider configuration";
                                logger.warn("Social login failed: code={}, detail={}",
                                        oauth.getError().getErrorCode(), description);
                            } else {
                                logger.warn("Social login failed: {}", exception.getClass().getSimpleName());
                            }
                            new org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler("/login?error")
                                    .onAuthenticationFailure(request, response, exception);
                        })
                        .userInfoEndpoint(userInfo ->
                                userInfo
                                        .oidcUserService(oidcUserService)
                                        .userService(oauth2UserService)
                        )
                );

        return http.build();
    }

    static void auditLogin(
            RecordLoginAttemptUseCase useCase,
            String username,
            boolean success
    ) {
        if (username == null || username.isBlank()) {
            return;
        }

        try {
            useCase.execute(username, success);
        } catch (ResourceNotFoundException ignored) {
        } catch (RuntimeException exception) {
            org.slf4j.LoggerFactory.getLogger(ApiSecurityConfig.class)
                    .warn("Could not record login audit: {}",
                            exception.getClass().getSimpleName());
        }
    }
}
