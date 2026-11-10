package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.CurrentActorProvider;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.entity.Role;

import java.time.Clock;
import java.util.UUID;

public final class DeleteRoleUseCase {
    private final RoleRepository roles;
    private final CurrentActorProvider actorProvider;
    private final Clock clock;

    public DeleteRoleUseCase(
            RoleRepository roles,
            CurrentActorProvider actorProvider,
            Clock clock
    ) {
        this.roles = roles;
        this.actorProvider = actorProvider;
        this.clock = clock;
    }

    public void execute(UUID uuid) {
        Role role = roles.findByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role", uuid));

        role.softDelete(actorProvider.currentActor(), clock.instant());
        roles.save(role);
    }
}
