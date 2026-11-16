package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.DeactivationResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;

import java.time.Clock;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ManageDeactivationUseCase {
    private final AccountSecurityRepository accounts;
    private final Clock clock;

    public ManageDeactivationUseCase(AccountSecurityRepository accounts, Clock clock) {
        this.accounts = Objects.requireNonNull(accounts);
        this.clock = Objects.requireNonNull(clock);
    }

    public DeactivationResult getPending(UUID userId) {
        return accounts.pendingDeactivation(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Deactivation request", userId));
    }

    public DeactivationResult request(UUID userId, String reason) {
        if (reason != null && !Set.of("not_needed", "other_account", "privacy", "other").contains(reason)) {
            throw new IllegalArgumentException("Invalid deactivation reason");
        }
        return accounts.requestDeactivation(userId, reason, clock.instant());
    }

    public void cancel(UUID userId) {
        accounts.cancelDeactivation(userId, clock.instant());
    }
}
