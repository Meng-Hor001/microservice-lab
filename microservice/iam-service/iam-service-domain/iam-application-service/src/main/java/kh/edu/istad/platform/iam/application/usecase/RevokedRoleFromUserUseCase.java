package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.UserRoleAssignmentRepository;

import java.time.Clock;
import java.util.UUID;

public final class RevokedRoleFromUserUseCase {

    private final UserRoleAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public RevokedRoleFromUserUseCase(UserRoleAssignmentRepository assignments, CurrentActorProvider actorProvider, Clock clock) {
        this.assignments = assignments;
        this.actorProvider = actorProvider;
        this.clock = clock;
    }

    public void execute(UUID userUuid, UUID roleUuid, String reason) {
        var assignment = assignments
                .findActiveByUserUuidAndRoleUuid(userUuid, roleUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User role assignment", roleUuid
                ));

        assignment.revoke(
                actorProvider.currentActor(),
                reason,
                clock.instant()
        );

        assignments.save(assignment);
    }

}
