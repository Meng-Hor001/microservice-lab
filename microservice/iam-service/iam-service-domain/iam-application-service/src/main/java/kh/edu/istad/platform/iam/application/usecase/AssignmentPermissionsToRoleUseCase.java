package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.AssignPermissionsToRoleCommand;
import kh.edu.istad.platform.iam.application.dto.PermissionAssignmentResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.application.port.out.RolePermissionAssignmentRepository;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.entity.Permission;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.RolePermission;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

public final class AssignmentPermissionsToRoleUseCase {
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final RolePermissionAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public AssignmentPermissionsToRoleUseCase(RoleRepository roles, PermissionRepository permissions, RolePermissionAssignmentRepository assignments, CurrentActorProvider actorProvider, Clock clock) {
        this.roles = roles;
        this.permissions = permissions;
        this.assignments = assignments;
        this.actorProvider = actorProvider;
        this.clock = clock;
    }


    public List<PermissionAssignmentResult> execute(
            AssignPermissionsToRoleCommand command
    ) {
        Objects.requireNonNull(command, "Command is required");

        Role role = roles.findByUuid(command.roleUuid())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role", command.roleUuid()));

        role.ensureAssignable();

        String actor = actorProvider.currentActor();
        var now = clock.instant();

        List<RolePermission> newAssignments = command.permissionUuids()
                .stream()
                .map(uuid -> {
                    Permission permission = permissions.findByUuid(uuid)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Permission", uuid));

                    return RolePermission.assign(
                            role,
                            permission,
                            actor,
                            now,
                            command.expiresAt(),
                            command.reason()
                    );
                })
                .toList();

        return assignments.saveAll(newAssignments).stream()
                .map(a -> new PermissionAssignmentResult(
                        command.roleUuid(),
                        command.permissionUuids().stream()
                                .filter(uuid -> permissions.findByUuid(uuid)
                                        .map(p -> p.getId().equals(a.getPermissionId()))
                                        .orElse(false))
                                .findFirst()
                                .orElseThrow(),
                        a.getAssignedAt(),
                        a.getAssignedBy(),
                        a.getExpiresAt(),
                        a.getRevokedAt(),
                        a.getRevokedBy(),
                        a.getReason()
                ))
                .toList();
    }
}
