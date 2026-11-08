package kh.edu.istad.platform.customer.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class CustomerSecurityConfig {

//    @Bean
//    SecurityWebFilterChain customerSecurity(ServerHttpSecurity http) {
//        var converter = new ReactiveJwtAuthenticationConverter();
//
//        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
//            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
//
//            if (realmAccess == null
//                    || !(realmAccess.get("roles") instanceof List<?> roles)) {
//                return Flux.empty();
//            }
//
//            return Flux.fromIterable(roles)
//                    .ofType(String.class)
//                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
//        });
//
//        return http
//                .csrf(ServerHttpSecurity.CsrfSpec::disable)
//                .authorizeExchange(auth -> auth
//                        .pathMatchers("/actuator/health", "/actuator/health/**")
//                        .permitAll()
//                        .pathMatchers(HttpMethod.GET, "/api/customers/**")
//                        .hasAuthority("SCOPE_customers.read")
//                        .anyExchange().denyAll())
//                .oauth2ResourceServer(oauth -> oauth
//                        .jwt(Customizer.withDefaults()))
//                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
//                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
//                .build();
//    }
}
