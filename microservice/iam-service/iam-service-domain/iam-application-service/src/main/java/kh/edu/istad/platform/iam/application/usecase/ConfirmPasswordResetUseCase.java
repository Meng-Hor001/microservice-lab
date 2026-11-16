package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;

import java.time.Clock;
import java.util.Objects;

public final class ConfirmPasswordResetUseCase {
    private final AccountSecurityRepository accounts;
    private final PasswordHasher hasher;
    private final Clock clock;

    public ConfirmPasswordResetUseCase(AccountSecurityRepository accounts, PasswordHasher hasher, Clock clock) {
        this.accounts = Objects.requireNonNull(accounts);
        this.hasher = Objects.requireNonNull(hasher);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(String token, String password, String confirmation) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }
        if (password == null || password.length() < 8 || password.length() > 64
                || !password.matches("(?s).*[a-z].*") || !password.matches("(?s).*[A-Z].*")
                || !password.matches("(?s).*\\d.*") || !password.matches("(?s).*[^A-Za-z0-9].*")) {
            throw new IllegalArgumentException("Password must be 8-64 characters with upper/lowercase letters, a number and a symbol");
        }
        if (!password.equals(confirmation)) {
            throw new IllegalArgumentException("New passwords do not match");
        }
        accounts.confirmPasswordReset(RequestPasswordResetUseCase.hashToken(token), hasher.hash(password), clock.instant());
    }
}
