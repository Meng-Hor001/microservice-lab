package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.AssignRolesToUserCommand;
import kh.edu.istad.platform.iam.application.dto.RoleAssignmentResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRepository;
import kh.edu.istad.platform.iam.application.port.out.UserRoleAssignmentRepository;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.entity.User;
import kh.edu.istad.platform.iam.domain.entity.UserRole;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AssignRolesToUserUseCase {
    private final UserRepository users;
    private final RoleRepository roles;
    private final UserRoleAssignmentRepository assignments;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public AssignRolesToUserUseCase(UserRepository users, RoleRepository roles, UserRoleAssignmentRepository assignments, CurrentActorProvider actorProvider, Clock clock) {
        this.users = users;
        this.roles = roles;
        this.assignments = assignments;
        this.actorProvider = actorProvider;
        this.clock = clock;
    }
    public List<RoleAssignmentResult> execute(AssignRolesToUserCommand command) {
        Objects.requireNonNull(command, "Command is required");

        User user = users.findByUuid(command.userUuid())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", command.userUuid()));

        String actor = actorProvider.currentActor();
        var now = clock.instant();

        List<UserRole> newAssignments = command.roleUuids().stream()
                .map(roleUuid -> {
                    Role role = roles.findByUuid(roleUuid)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Role", roleUuid));

                    role.ensureAssignable();

                    return UserRole.assign(
                            user,
                            role,
                            actor,
                            now,
                            command.expiresAt(),
                            command.reason()
                    );
                })
                .toList();

        List<UserRole> savedAssignments = assignments.saveAll(newAssignments);
        List<UUID> roleUuids = List.copyOf(command.roleUuids());
        List<RoleAssignmentResult> results = new ArrayList<>();
        for (int i = 0; i < savedAssignments.size(); i++) {
            UserRole assignment = savedAssignments.get(i);

            results.add(new RoleAssignmentResult(
                    command.userUuid(),
                    roleUuids.get(i),
                    assignment.getAssignedAt(),
                    assignment.getAssignedBy(),
                    assignment.getExpiresAt(),
                    assignment.getRevokedAt(),
                    assignment.getRevokedBy(),
                    assignment.getReason()
            ));
        }
        return List.copyOf(results);
    }
}
