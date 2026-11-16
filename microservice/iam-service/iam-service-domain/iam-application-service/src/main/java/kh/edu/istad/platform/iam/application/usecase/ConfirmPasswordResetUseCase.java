package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.support.PasswordPolicy;
import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;

import java.time.Clock;
import java.util.Objects;

public final class ConfirmPasswordResetUseCase {
    private final AccountSecurityRepository accounts;
    private final PasswordHasher hasher;
    private final PasswordPolicy passwordPolicy;
    private final Clock clock;

    public ConfirmPasswordResetUseCase(AccountSecurityRepository accounts, PasswordHasher hasher,
                                       PasswordPolicy passwordPolicy, Clock clock) {
        this.passwordPolicy = Objects.requireNonNull(passwordPolicy);
        this.accounts = Objects.requireNonNull(accounts);
        this.hasher = Objects.requireNonNull(hasher);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(String token, String password, String confirmation) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }
        passwordPolicy.checked(password);
        if (!password.equals(confirmation)) {
            throw new IllegalArgumentException("New passwords do not match");
        }
        accounts.confirmPasswordReset(RequestPasswordResetUseCase.hashToken(token), hasher.hash(password), clock.instant());
    }
}
