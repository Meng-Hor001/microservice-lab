package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.UserPermissionAssignmentRepository;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public final class RevokePermissionFromUserUseCase {
    private final UserPermissionAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public RevokePermissionFromUserUseCase(
            UserPermissionAssignmentRepository assignments,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.assignments = Objects.requireNonNull(assignments);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public void execute(UUID userUuid, UUID permissionUuid, String reason) {
        var assignment = assignments
                .findActiveByUserUuidAndPermissionUuid(userUuid, permissionUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User permission assignment", permissionUuid
                ));

        assignment.revoke(
                actorProvider.currentActor(),
                reason,
                clock.instant()
        );

        assignments.save(assignment);
    }

}
