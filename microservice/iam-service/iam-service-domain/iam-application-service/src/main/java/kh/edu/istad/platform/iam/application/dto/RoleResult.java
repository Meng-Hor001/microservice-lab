package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.Role;

import java.util.UUID;

public record RoleResult(
        UUID uuid,
        String name,
        String description,
        boolean system,
        boolean active,
        long version
) {

    public static RoleResult from(Role role) {
        return new RoleResult(
                role.getUuid(),
                role.getName(),
                role.getDescription(),
                role.isSystem(),
                role.isActive(),
                role.getVersion());
    }
}
