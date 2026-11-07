package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.PermissionResult;
import kh.edu.istad.platform.iam.application.dto.UpdatePermissionCommand;
import kh.edu.istad.platform.iam.application.exception.DuplicatePermissionException;
import kh.edu.istad.platform.iam.application.exception.VersionConflictException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.application.support.PermissionLookup;
import kh.edu.istad.platform.iam.domain.entity.Permission;

import java.time.Clock;
import java.util.Objects;

public final class UpdatePermissionUseCase {
    private final PermissionRepository repository;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public UpdatePermissionUseCase(
            PermissionRepository repository,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public PermissionResult execute(UpdatePermissionCommand command) {
        Objects.requireNonNull(command, "Command is required");

        Permission permission = PermissionLookup.require(
                repository,
                command.id()
        );

        if (permission.getVersion() != command.version()) {
            throw new VersionConflictException("Permission");
        }

        String previousName = permission.getName();

        permission.update(
                command.resource(),
                command.action(),
                command.description(),
                actorProvider.currentActor(),
                clock.instant()
        );

        if (!previousName.equals(permission.getName())
                && repository.existsByName(permission.getName())) {
            throw new DuplicatePermissionException(
                    permission.getName()
            );
        }

        return PermissionResult.from(repository.save(permission));
    }
}
