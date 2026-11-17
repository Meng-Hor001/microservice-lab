package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record RoleSummary(
        UUID uuid,
        String name,
        String description,
        boolean system,
        boolean active,
        long permissionCount,
        long userCount,
        Instant updatedAt,
        String updatedBy
) {

    public static RoleSummary from(Role role, long permissionCount, long userCount) {
        return new RoleSummary(
                role.getUuid(),
                role.getName(),
                role.getDescription(),
                role.isSystem(),
                role.isActive(),
                permissionCount,
                userCount,
                role.getUpdatedAt(),
                role.getUpdatedBy()
        );
    }
}
