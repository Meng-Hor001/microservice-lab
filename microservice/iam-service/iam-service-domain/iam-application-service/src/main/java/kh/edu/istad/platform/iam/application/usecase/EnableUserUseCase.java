package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.User;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public final  class EnableUserUseCase {
    private final UserRepository users;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public EnableUserUseCase(
            UserRepository users,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.users = Objects.requireNonNull(users);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(UUID userUuid) {
        User user = users.findByUuid(userUuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", userUuid));

        user.enable(actorProvider.currentActor(), clock.instant());
        users.save(user);
    }
}
