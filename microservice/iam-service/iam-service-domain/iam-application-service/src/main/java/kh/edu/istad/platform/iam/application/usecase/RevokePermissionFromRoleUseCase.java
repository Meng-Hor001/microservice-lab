package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.application.port.out.RolePermissionAssignmentRepository;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;

import java.time.Clock;
import java.util.UUID;

public final class RevokePermissionFromRoleUseCase {
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final RolePermissionAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public RevokePermissionFromRoleUseCase(
            RoleRepository roles,
            PermissionRepository permissions,
            RolePermissionAssignmentRepository assignments,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.roles = roles;
        this.permissions = permissions;
        this.assignments = assignments;
        this.actorProvider = actorProvider;
        this.clock = clock;
    }

    public void execute(UUID roleUuid, UUID permissionUuid, String reason) {
        var role = roles.findByUuid(roleUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleUuid));

        var permission = permissions.findByUuid(permissionUuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Permission", permissionUuid));

        RolePermission assignment = assignments
                .findActiveByRoleUuid(roleUuid)
                .stream()
                .filter(a -> a.getPermissionId().equals(permission.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role permission assignment",
                        permissionUuid
                ));

        assignment.revoke(
                role,
                actorProvider.currentActor(),
                reason,
                clock.instant()
        );

        assignments.save(assignment);
    }
}
