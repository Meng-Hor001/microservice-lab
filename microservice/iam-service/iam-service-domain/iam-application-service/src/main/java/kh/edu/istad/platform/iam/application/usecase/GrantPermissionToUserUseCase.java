package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;

import kh.edu.istad.platform.iam.application.dto.PermissionGrantResult;
import kh.edu.istad.platform.iam.application.port.out.PermissionRepository;
import kh.edu.istad.platform.iam.application.port.out.UserPermissionAssignmentRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserPermission;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

public final class GrantPermissionToUserUseCase {
    private final UserRepository users;
    private final PermissionRepository permissions;
    private final UserPermissionAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public GrantPermissionsToUserUseCase(
            UserRepository users,
            PermissionRepository permissions,
            UserPermissionAssignmentRepository assignments,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.users = users;
        this.permissions = permissions;
        this.assignments = assignments;
        this.actorProvider = actorProvider;
        this.clock = clock;
    }

    public List<PermissionGrantResult> execute(GrantPermissionsToUserCommand command) {
        Objects.requireNonNull(command, "Command is required");

        User user = users.findByUuid(command.userUuid())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", command.userUuid()));

        String actor = actorProvider.currentActor();
        var now = clock.instant();

        List<UserPermission> newAssignments = command.permissionUuids().stream()
                .map(permissionUuid -> {
                    Permission permission = permissions.findByUuid(permissionUuid)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Permission", permissionUuid));

                    permission.ensureAssignable();

                    return UserPermission.assign(
                            user,
                            permission,
                            command.effect(),
                            actor,
                            now,
                            command.expiresAt(),
                            command.reason()
                    );
                })
                .toList();

        return assignments.saveAll(newAssignments).stream()
                .map(assignment -> new PermissionGrantResult(
                        command.userUuid(),
                        assignment.getPermissionId(),
                        assignment.getEffect(),
                        assignment.getAssignedAt(),
                        assignment.getAssignedBy(),
                        assignment.getExpiresAt(),
                        assignment.getRevokedAt(),
                        assignment.getRevokedBy(),
                        assignment.getReason()
                ))
                .toList();
    }
}
