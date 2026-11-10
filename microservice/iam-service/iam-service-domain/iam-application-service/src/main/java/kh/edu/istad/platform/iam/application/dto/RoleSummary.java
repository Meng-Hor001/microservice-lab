package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.Role;

import java.util.UUID;

public record RoleSummary(
        UUID uuid,
        String name,
        String description,
        boolean active
) {

    public static RoleSummary from(Role role) {
        return new RoleSummary(
                role.getUuid(),
                role.getName(),
                role.getDescription(),
                role.isActive()
        );
    }
}
