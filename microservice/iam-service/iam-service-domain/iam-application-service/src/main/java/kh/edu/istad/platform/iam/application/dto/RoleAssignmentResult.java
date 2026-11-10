package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;
import java.util.UUID;

public record RoleAssignmentResult(
        UUID userUuid,
        UUID roleUuid,
        Instant assignedAt,
        String assignedBy,
        Instant expiresAt,
        Instant revokedAt,
        String revokedBy,
        String reason
) {
}
