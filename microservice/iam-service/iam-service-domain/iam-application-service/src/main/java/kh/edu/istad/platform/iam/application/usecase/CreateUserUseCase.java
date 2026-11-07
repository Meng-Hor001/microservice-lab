package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.CreateUserCommand;
import kh.edu.istad.platform.iam.application.dto.UserResult;
import kh.edu.istad.platform.iam.application.exception.DuplicateUserException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.PasswordHasher;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.User;

import java.time.Clock;
import java.util.Objects;

public final class CreateUserUseCase {
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public CreateUserUseCase(
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

    public UserResult execute(CreateUserCommand command) {
        Objects.requireNonNull(command, "Command is required");
        Objects.requireNonNull(command.profile(), "User profile is required");

        String username = Objects.requireNonNull(command.username(), "Username is required").trim();
        String email = command.profile().email();
        if (users.existsByUsername(username)) {
            throw new DuplicateUserException("username", username);
        }
        if (users.existsByEmail(email)) {
            throw new DuplicateUserException("email", email);
        }

        var now = clock.instant();
        String actor = actorProvider.currentActor();
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordHasher.hash(command.password()))
                .givenName(command.profile().givenName())
                .familyName(command.profile().familyName())
                .middleName(command.profile().middleName())
                .nickname(command.profile().nickname())
                .gender(command.profile().gender())
                .birthdate(command.profile().birthdate())
                .profileUrl(command.profile().profileUrl())
                .pictureUrl(command.profile().pictureUrl())
                .websiteUrl(command.profile().websiteUrl())
                .zoneinfo(command.profile().zoneinfo())
                .locale(command.profile().locale())
                .phoneNumber(command.profile().phoneNumber())
                .addressStreetAddress(command.profile().addressStreetAddress())
                .addressLocality(command.profile().addressLocality())
                .addressRegion(command.profile().addressRegion())
                .addressPostalCode(command.profile().addressPostalCode())
                .addressCountry(command.profile().addressCountry())
                .createdAt(now)
                .createdBy(actor)
                .build();

        return UserResult.from(users.save(user));
    }
}
