package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.IdentityRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public final class UnlinkIdentityUseCase {
    private final UserRepository users;
    private final IdentityRepository identities;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public UnlinkIdentityUseCase(
            UserRepository users,
            IdentityRepository identities,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.users = Objects.requireNonNull(users);
        this.identities = Objects.requireNonNull(identities);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(UUID userUuid, UUID identityUuid) {
        var user = users.findByUuid(userUuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", userUuid));

        var existingIdentities = identities.findByUserUuid(userUuid);

        var identity = existingIdentities.stream()
                .filter(candidate -> candidate.getUuid().equals(identityUuid))
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("Identity", identityUuid));

        identity.unlink(
                user,
                existingIdentities,
                actorProvider.currentActor(),
                clock.instant()
        );

        identities.save(identity);
    }
}
