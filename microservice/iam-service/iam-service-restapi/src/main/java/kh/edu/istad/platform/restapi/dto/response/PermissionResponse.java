package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.PermissionResult;
import java.time.Instant;
import java.util.UUID;

public record PermissionResponse(
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
    public static PermissionResponse from(PermissionResult permission) {
        return new PermissionResponse(
                permission.uuid(), permission.name(), permission.resource(), permission.action(),
                permission.description(), permission.system(), permission.active(),
                permission.createdAt(), permission.updatedAt(), permission.version()
        );
    }
}
