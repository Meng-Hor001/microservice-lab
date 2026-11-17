package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.Permission;

import java.time.Instant;
import java.util.UUID;

/** A permission as shown in the list, with how many roles currently grant it. */
public record PermissionSummary(
        UUID uuid,
        String name,
        String resource,
        String action,
        String description,
        boolean system,
        boolean active,
        long roleCount,
        Instant createdAt,
        Instant updatedAt,
        String updatedBy,
        long version
) {
    public static PermissionSummary from(Permission permission, long roleCount) {
        return new PermissionSummary(
                permission.getUuid(),
                permission.getName(),
                permission.getResource(),
                permission.getAction(),
                permission.getDescription(),
                permission.isSystem(),
                permission.isActive(),
                roleCount,
                permission.getCreatedAt(),
                permission.getUpdatedAt(),
                permission.getUpdatedBy(),
                permission.getVersion()
        );
    }
}
