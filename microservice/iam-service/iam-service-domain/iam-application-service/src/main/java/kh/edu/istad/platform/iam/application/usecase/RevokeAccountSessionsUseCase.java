package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public final class RevokeAccountSessionsUseCase {
    private final AccountSecurityRepository accounts;
    private final Clock clock;

    public RevokeAccountSessionsUseCase(AccountSecurityRepository accounts, Clock clock) {
        this.accounts = Objects.requireNonNull(accounts);
        this.clock = Objects.requireNonNull(clock);
    }

    public void revokeOne(UUID userId, UUID sessionId) {
        if (accounts.revoke(userId, Objects.requireNonNull(sessionId), null, clock.instant()) == 0) {
            throw new ResourceNotFoundException("Active session", sessionId);
        }
    }

    public int revokeOthers(UUID userId, String currentSessionId) {
        return accounts.revoke(userId, null, sessionId(currentSessionId), clock.instant());
    }

    public int revokeAll(UUID userId) {
        return accounts.revoke(userId, null, null, clock.instant());
    }

    static UUID sessionId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The access token has no account session; sign in again");
        }
        return UUID.fromString(value);
    }
}
