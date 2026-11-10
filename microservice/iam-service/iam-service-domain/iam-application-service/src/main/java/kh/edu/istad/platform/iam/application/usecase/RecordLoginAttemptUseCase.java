package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;

import java.time.Clock;
import java.util.Objects;

public final class RecordLoginAttemptUseCase {
    private final UserRepository users;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public RecordLoginAttemptUseCase(
            UserRepository users,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.users = Objects.requireNonNull(users);
        this.actorProvider = Objects.requireNonNull(actorProvider);
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
        user.recordLoginAttempt(success, actorProvider.currentActor(), clock.instant());
        users.save(user);
    }
}
