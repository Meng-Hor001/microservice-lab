package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.SystemSettingsResult;
import kh.edu.istad.platform.iam.application.dto.UpdateSystemSettingsCommand;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.SystemSettingsRepository;

import java.time.Clock;
import java.util.Objects;

/** Admin view and update of platform-wide authentication, password, session and token policies. */
public final class ManageSystemSettingsUseCase {
    private final SystemSettingsRepository settings;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public ManageSystemSettingsUseCase(SystemSettingsRepository settings, CurrentActorProvider actorProvider, Clock clock) {
        this.settings = Objects.requireNonNull(settings);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public SystemSettingsResult get() {
        return settings.get();
    }

    public SystemSettingsResult update(UpdateSystemSettingsCommand command) {
        validate(Objects.requireNonNull(command));
        return settings.update(command, actorProvider.currentActor(), clock.instant());
    }

    // Cross-field rules that single-field annotations cannot express.
    private static void validate(UpdateSystemSettingsCommand c) {
        if ((long) c.sessionIdleMinutes() > c.sessionMaxHours() * 60L) {
            throw new IllegalArgumentException("Session idle timeout cannot be longer than the maximum session length");
        }
        if ((long) c.accessTokenMinutes() > c.refreshTokenDays() * 24L * 60L) {
            throw new IllegalArgumentException("Access tokens cannot live longer than refresh tokens");
        }
        int required = (c.passwordRequireUppercase() ? 1 : 0) + (c.passwordRequireLowercase() ? 1 : 0)
                + (c.passwordRequireDigit() ? 1 : 0) + (c.passwordRequireSymbol() ? 1 : 0);
        if (c.passwordMinLength() < required) {
            throw new IllegalArgumentException("Minimum password length is shorter than the required character types");
        }
    }
}
