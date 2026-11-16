package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

public final class RequestPasswordResetUseCase {
    private final UserRepository users;
    private final AccountSecurityRepository accounts;
    private final PasswordResetDelivery delivery;
    private final TransactionRunner transactions;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public RequestPasswordResetUseCase(UserRepository users, AccountSecurityRepository accounts,
                                      PasswordResetDelivery delivery, TransactionRunner transactions, Clock clock) {
        this.users = Objects.requireNonNull(users);
        this.accounts = Objects.requireNonNull(accounts);
        this.delivery = Objects.requireNonNull(delivery);
        this.transactions = Objects.requireNonNull(transactions);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(UUID userId) {
        transactions.execute(() -> {
            var user = users.findByUuid(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            var now = clock.instant();
            accounts.savePasswordReset(userId, hashToken(token), now.plus(Duration.ofMinutes(15)), now);
            delivery.send(user.getEmail(), token);
            return null;
        });
    }

    public static String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
