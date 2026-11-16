package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.SuspiciousLoginResult;
import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/** Users with many failed sign-ins in a recent window (possible brute force). */
public final class ListSuspiciousLoginsUseCase {
    private static final int MAX_RESULTS = 100;

    private final AccountSecurityRepository accounts;
    private final Clock clock;

    public ListSuspiciousLoginsUseCase(AccountSecurityRepository accounts, Clock clock) {
        this.accounts = Objects.requireNonNull(accounts);
        this.clock = Objects.requireNonNull(clock);
    }

    public List<SuspiciousLoginResult> execute(int days, int threshold) {
        if (days < 1 || days > 365 || threshold < 1 || threshold > 1000) {
            throw new IllegalArgumentException("Invalid suspicious login filters");
        }
        return accounts.suspiciousLogins(clock.instant().minus(days, ChronoUnit.DAYS), threshold, MAX_RESULTS);
    }
}
