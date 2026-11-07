package kh.edu.istad.platform.iam.application.usecase;

import kh.edu.istad.platform.iam.application.dto.RoleResult;
import kh.edu.istad.platform.iam.application.exception.ResourceNotFoundException;
import kh.edu.istad.platform.iam.application.port.out.RoleRepository;
import kh.edu.istad.platform.iam.domain.entity.Role;

import java.util.Objects;
import java.util.UUID;


public final class GetRoleUseCase {

    private final RoleRepository roles;

    public GetRoleUseCase(RoleRepository roles) {
        this.roles = Objects.requireNonNull(roles);
    }

    public RoleResult execute(UUID uuid) {
        Objects.requireNonNull(uuid, "Role UUID is required");

        Role role = roles.findByUuid(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role", uuid));

        return RoleResult.from(role);
    }


}
