package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.PermissionGrantResult;

import java.time.Instant;
import java.util.UUID;

public record PermissionGrantResponse(
        UUID userUuid,
        UUID permissionUuid,
        String effect,
        Instant assignedAt,
        String assignedBy,
        Instant expiresAt,
        Instant revokedAt,
        String revokedBy,
        String reason
) {
    public static PermissionGrantResponse from(PermissionGrantResult grant) {
        return new PermissionGrantResponse(
                grant.userUuid(), grant.permissionUuid(), grant.effect().name(),
                grant.assignedAt(), grant.assignedBy(), grant.expiresAt(),
                grant.revokedAt(), grant.revokedBy(), grant.reason()
        );
    }
}
