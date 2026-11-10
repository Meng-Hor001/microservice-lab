package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.RoleAssignmentResult;

import java.time.Instant;
import java.util.UUID;

public record RoleAssignmentResponse(
        UUID userUuid,
        UUID roleUuid,
        Instant assignedAt,
        String assignedBy,
        Instant expiresAt,
        Instant revokedAt,
        String revokedBy,
        String reason
) {
    public static RoleAssignmentResponse from(RoleAssignmentResult assignment) {
        return new RoleAssignmentResponse(
                assignment.userUuid(), assignment.roleUuid(), assignment.assignedAt(),
                assignment.assignedBy(), assignment.expiresAt(), assignment.revokedAt(),
                assignment.revokedBy(), assignment.reason()
        );
    }
}
