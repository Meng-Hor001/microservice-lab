package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.enums.PermissionEffect;

import java.time.Instant;
import java.util.UUID;

public record PermissionGrantResult(
        UUID userId,
        Long permissionId,
        PermissionEffect effect,
        Instant assignedAt,
        String assignedBy,
        Instant expiredAt,
        Instant revokedAt,
        String revokedBy,
        String reason
) {
}
