package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.enums.PermissionEffect;

import java.time.Instant;
import java.util.UUID;

public record PermissionGrantResult(
        UUID userUuid,
        UUID permissionUuid,
        PermissionEffect effect,
        Instant assignedAt,
        String assignedBy,
        Instant expiresAt,
        Instant revokedAt,
        String revokedBy,
        String reason
) {
}
