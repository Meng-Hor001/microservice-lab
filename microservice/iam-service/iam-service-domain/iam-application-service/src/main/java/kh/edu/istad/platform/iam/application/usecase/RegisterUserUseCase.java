package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.RegisterUserCommand;
import kh.edu.istad.platform.iam.application.dto.UserResult;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;

import java.time.Clock;
import java.util.Locale;
import java.util.Objects;

public final class RegisterUserUseCase {
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public RegisterUserUseCase(
            UserRepository users,
            PasswordHasher passwordHasher,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.users = Objects.requireNonNull(users);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public UserResult execute(RegisterUserCommand command) {
        Objects.requireNonNull(command, "Command is required");

        if (!Objects.equals(command.password(), command.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        String username = command.username().trim();
        String email = command.email().trim().toLowerCase(Locale.ROOT);

        if (users.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        var now = clock.instant();
        String actor = actorProvider.currentActor();

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordHasher.hash(command.password()))
                .givenName(command.givenName())
                .familyName(command.familyName())
                .locale(command.locale())
                .createdAt(now)
                .createdBy(actor)
                .build();

        return UserResult.from(users.save(user));
    }
}
