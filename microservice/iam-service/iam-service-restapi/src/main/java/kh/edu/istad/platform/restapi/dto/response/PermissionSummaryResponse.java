package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.PermissionSummary;

import java.time.Instant;
import java.util.UUID;

public record PermissionSummaryResponse(
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
    public static PermissionSummaryResponse from(PermissionSummary permission) {
        return new PermissionSummaryResponse(
                permission.uuid(), permission.name(), permission.resource(), permission.action(),
                permission.description(), permission.system(), permission.active(), permission.roleCount(),
                permission.createdAt(), permission.updatedAt(), permission.updatedBy(), permission.version()
        );
    }
}
