package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.RoleResult;
import kh.edu.istad.platform.iam.application.dto.UpdateRoleCommand;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.exception.VersionConflictException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.entity.Role;
import kh.edu.istad.platform.iam.domain.exception.DuplicateRoleException;

import java.time.Clock;
import java.util.Objects;

public final class UpdateRoleUseCase {
    private final RoleRepository roles;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public UpdateRoleUseCase(
            RoleRepository roles,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.roles = Objects.requireNonNull(roles);
        this.actorProvider = Objects.requireNonNull(actorProvider);
        this.clock = Objects.requireNonNull(clock);
    }

    public RoleResult execute(UpdateRoleCommand command) {
        Objects.requireNonNull(command, "Command is required");

        Role role = roles.findByUuid(command.uuid())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role", command.uuid()));

        if (role.getVersion() != command.version()) {
            throw new VersionConflictException("Role");
        }

        if (!role.getName().equals(command.name())
                && roles.existsByName(command.name())) {
            throw new DuplicateRoleException(command.name());
        }

        String actor = actorProvider.currentActor();
        var now = clock.instant();

        role.rename(command.name(), actor, now);
        role.updateDescription(command.description(), actor, now);

        return RoleResult.from(roles.save(role));
    }
}
