package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.AccountSessionResult;
import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ListAccountSessionsUseCase {
    private final AccountSecurityRepository accounts;
    private final Clock clock;

    public ListAccountSessionsUseCase(AccountSecurityRepository accounts, Clock clock) {
        this.accounts = Objects.requireNonNull(accounts);
        this.clock = Objects.requireNonNull(clock);
    }

    public List<AccountSessionResult> execute(UUID userId, String currentSessionId) {
        return accounts.sessions(userId, RevokeAccountSessionsUseCase.sessionId(currentSessionId), clock.instant());
    }

    public List<AccountSessionResult> executeForUser(UUID userId){
        return accounts.sessions(Objects.requireNonNull(userId), null, clock.instant());
    }
}
