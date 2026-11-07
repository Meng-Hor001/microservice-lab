package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.entity.Permission;

import java.time.Instant;
import java.util.UUID;

public record PermissionResult(
        UUID uuid,
        String name,
        String resource,
        String action,
        String description,
        boolean system,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
    public static PermissionResult from(Permission permission) {
        return new PermissionResult(
                permission.getUuid(),
                permission.getName(),
                permission.getResource(),
                permission.getAction(),
                permission.getDescription(),
                permission.isSystem(),
                permission.isActive(),
                permission.getCreatedAt(),
                permission.getUpdatedAt(),
                permission.getVersion()
        );
    }
}
