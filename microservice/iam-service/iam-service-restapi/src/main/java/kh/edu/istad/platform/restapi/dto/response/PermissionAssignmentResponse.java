package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.PermissionAssignmentResult;

import java.time.Instant;
import java.util.UUID;

public record PermissionAssignmentResponse(
        UUID roleUuid,
        UUID permissionUuid,
        Instant assignedAt,
        String assignedBy,
        Instant expiresAt,
        Instant revokedAt,
        String revokedBy,
        String reason
) {
    public static PermissionAssignmentResponse from(PermissionAssignmentResult assignment) {
        return new PermissionAssignmentResponse(
                assignment.roleUuid(), assignment.permissionUuid(), assignment.assignedAt(),
                assignment.assignedBy(), assignment.expiresAt(), assignment.revokedAt(),
                assignment.revokedBy(), assignment.reason()
        );
    }
}
