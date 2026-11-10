package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.AssignPermissionsToRoleCommand;
import kh.edu.istad.platform.iam.application.dto.CreateRoleCommand;
import kh.edu.istad.platform.iam.application.dto.RoleResult;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.application.port.out.TransactionRunner;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.exception.DuplicateRoleException;

import java.time.Clock;
import java.util.Objects;


public final class CreateRoleUseCase {
    private final AssignmentPermissionsToRoleUseCase permissionsAssignments;
    private final RoleRepository roles;
    private final TransactionRunner transaction;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public CreateRoleUseCase(
            AssignmentPermissionsToRoleUseCase permissionsAssignments,
            RoleRepository roles, TransactionRunner transaction,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.permissionsAssignments = Objects.requireNonNull(permissionsAssignments);
        this.roles = Objects.requireNonNull(roles);
        this.transaction = Objects.requireNonNull(transaction);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public RoleResult execute(CreateRoleCommand command) {
        return transaction.execute(() -> createRole(command));
    }

    private RoleResult createRole(CreateRoleCommand command) {
        Objects.requireNonNull(command, "Command is required");

        if (roles.existsByName(command.name())) {
            throw new DuplicateRoleException(command.name());
        }

        Role role = Role.builder()
                .name(command.name())
                .description(command.description())
                .system(false)
                .active(true)
                .createdAt(clock.instant())
                .createdBy(actorProvider.currentActor())
                .build();

        Role savedRole = roles.save(role);

        if (!command.permissionUuids().isEmpty()) {
            permissionsAssignments.execute(
                    new AssignPermissionsToRoleCommand(
                            savedRole.getUuid(),
                            command.permissionUuids(),
                            null,
                            "Permission assigned at role creation"
                    )
            );
        }
        return RoleResult.from(savedRole);
    }
}
