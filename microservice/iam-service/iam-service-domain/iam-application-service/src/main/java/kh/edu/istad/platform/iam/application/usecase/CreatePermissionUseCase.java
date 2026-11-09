package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.CreatePermissionCommand;
import kh.edu.istad.platform.iam.application.dto.PermissionResult;
import kh.edu.istad.platform.iam.application.exception.DuplicatePermissionException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.domain.entity.Permission;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;


public final class CreatePermissionUseCase {
    private final PermissionRepository permissionRepository;
    private final CurrentActorProvider currentActorProvider;
    private final Clock clock;

    public CreatePermissionUseCase(
            PermissionRepository permissionRepository,
            CurrentActorProvider currentActorProvider,
            Clock clock
    ) {
        this.permissionRepository =
                Objects.requireNonNull(permissionRepository);

        this.currentActorProvider =
                Objects.requireNonNull(currentActorProvider);

        this.clock = Objects.requireNonNull(clock);
    }

    public PermissionResult execute(CreatePermissionCommand command) {
        Objects.requireNonNull(command, "Command is required");

        Instant now = clock.instant();
        String actor = currentActorProvider.currentActor();


        Permission permission = Permission.builder()
                .resource(command.resource())
                .action(command.action())
                .description(command.description())
                .system(false)
                .active(true)
                .createdAt(now)
                .createdBy(actor)
                .build();

        if (permissionRepository.existsByName(permission.getName())) {
            throw new DuplicatePermissionException(
                    permission.getName()
            );
        }

        Permission saved = permissionRepository.save(permission);

        return PermissionResult.from(saved);
    }


}
