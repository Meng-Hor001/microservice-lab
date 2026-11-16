package kh.edu.istad.platform.restapi.security;

import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Clock;
import java.util.UUID;

public final class AccountSessionTokenValidator implements OAuth2TokenValidator<Jwt> {
    private final AccountSecurityRepository accounts;
    private final Clock clock;

    public AccountSessionTokenValidator(AccountSecurityRepository accounts, Clock clock) {
        this.accounts = accounts;
        this.clock = clock;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        try {
            String sid = jwt.getClaimAsString("sid");
            if (sid != null && accounts.isSessionActive(UUID.fromString(jwt.getSubject()),
                    UUID.fromString(sid), clock.instant())) {
                return OAuth2TokenValidatorResult.success();
            }
        } catch (IllegalArgumentException ignored) {
            // Malformed subjects/session IDs must fail authentication.
        }
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "Account session is missing, expired or revoked", null));
    }
}
