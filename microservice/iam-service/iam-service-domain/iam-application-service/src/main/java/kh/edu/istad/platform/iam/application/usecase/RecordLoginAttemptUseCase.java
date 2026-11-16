package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.RecordAuditLogCommand;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.AccountSecurityRepository;
import kh.edu.istad.platform.iam.application.port.out.AuditLogRepository;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.SystemSettingsRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Records a sign-in attempt on the user. After a failed attempt it applies the authentication policy
 * from system settings: reaching the configured number of consecutive failures locks the account.
 */
public final class RecordLoginAttemptUseCase {
    private static final String SYSTEM_ACTOR = "system";

    private final UserRepository users;
    private final CurrentActorProvider actorProvider;
    private final SystemSettingsRepository settings;
    private final AccountSecurityRepository accounts;
    private final AuditLogRepository auditLogs;
    private final Clock clock;

    public RecordLoginAttemptUseCase(
            UserRepository users,
            CurrentActorProvider actorProvider,
            SystemSettingsRepository settings,
            AccountSecurityRepository accounts,
            AuditLogRepository auditLogs,
            Clock clock
    ) {
        this.users = Objects.requireNonNull(users);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.settings = Objects.requireNonNull(settings);
        this.accounts = Objects.requireNonNull(accounts);
        this.auditLogs = Objects.requireNonNull(auditLogs);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(String usernameOrEmail, boolean success) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            throw new IllegalArgumentException("Username or email is required");
        }
        var user = users.findByUsernameOrEmail(usernameOrEmail.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", usernameOrEmail
                ));
        Instant now = clock.instant();
        user.recordLoginAttempt(success, actorProvider.currentActor(), now);

        boolean lockedNow = false;
        int maxAttempts = 0;
        if (!success) {
            maxAttempts = settings.get().maxFailedAttempts();
            lockedNow = user.lockAfterFailedAttempts(maxAttempts, SYSTEM_ACTOR, now);
        }
        users.save(user);

        if (lockedNow) {
            String detail = "Locked after " + maxAttempts + " failed sign-ins";
            accounts.recordActivity(user.getUuid(), "account_locked", detail, now);
            auditLogs.save(new RecordAuditLogCommand(
                    SYSTEM_ACTOR, "USER_LOCKED", "USER", user.getUuid().toString(), detail, null, now));
        }
    }
}
