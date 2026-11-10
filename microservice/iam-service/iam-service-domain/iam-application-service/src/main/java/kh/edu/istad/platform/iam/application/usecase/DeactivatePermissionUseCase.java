package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.application.support.PermissionLookup;
import kh.edu.istad.platform.iam.domain.entity.Permission;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public final class DeactivatePermissionUseCase {
    private final PermissionRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public DeactivatePermissionUseCase(
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(UUID uuid) {
        Permission permission = PermissionLookup.require(repository, uuid);

        permission.deactivate(
                actorProvider.currentActor(),
                clock.instant()
        );

        repository.save(permission);
    }
}
