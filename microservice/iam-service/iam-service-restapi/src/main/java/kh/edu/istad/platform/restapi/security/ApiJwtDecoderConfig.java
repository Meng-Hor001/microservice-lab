package kh.edu.istad.platform.restapi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

import java.util.List;
import java.util.UUID;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ApiJwtDecoderConfig {
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.audiences[0]}") String audience
    ) {
        var decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        var audienceValidator = new JwtClaimValidator<List<String>>(
                "aud", values -> values != null && values.contains(audience)
        );
        var subjectValidator = new JwtClaimValidator<String>("sub", subject -> {
            if (subject == null) return false;
            try {
                return UUID.fromString(subject).toString().equalsIgnoreCase(subject);
            } catch (IllegalArgumentException exception) {
                return false;
            }
        });
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                audienceValidator,
                subjectValidator
        ));
        return decoder;
    }
}
