package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.ChangePasswordCommand;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.User;

import java.time.Clock;
import java.util.Objects;

public final class ChangePasswordUseCase {
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public ChangePasswordUseCase(
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

    public void execute(ChangePasswordCommand command) {
        Objects.requireNonNull(command, "Command is required");

        if (!Objects.equals(command.newPassword(), command.confirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        User user = users.findByUuid(command.userUuid())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", command.userUuid()));

        if (user.getPasswordHash() == null
                || !passwordHasher.matches(
                command.currentPassword(),
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.changePasswordHash(
                passwordHasher.hash(command.newPassword()),
                actorProvider.currentActor(),
                clock.instant()
        );

        users.save(user);
    }
}
