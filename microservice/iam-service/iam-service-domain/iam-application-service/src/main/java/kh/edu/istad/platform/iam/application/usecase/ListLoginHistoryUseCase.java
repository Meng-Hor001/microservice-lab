package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.LoginHistoryResult;
import kh.edu.istad.platform.iam.application.dto.PageResult;
import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ListLoginHistoryUseCase {
    private final AccountSecurityRepository accounts;
    private final Clock clock;

    public ListLoginHistoryUseCase(AccountSecurityRepository accounts, Clock clock) {
        this.accounts = Objects.requireNonNull(accounts);
        this.clock = Objects.requireNonNull(clock);
    }

    public PageResult<LoginHistoryResult> execute(UUID userId, String result, Integer days, int page, int size) {
        if (result == null || !Set.of("all", "success", "failed").contains(result)
                || page < 0 || size < 1 || size > 100 || (days != null && (days < 1 || days > 3650))) {
            throw new IllegalArgumentException("Invalid login history filters");
        }
        return accounts.loginHistory(userId, result,
                days == null ? null : clock.instant().minus(days, ChronoUnit.DAYS), page, size);
    }
}
