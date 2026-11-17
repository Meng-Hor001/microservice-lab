package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.RoleSummary;

import java.time.Instant;
import java.util.UUID;

public record RoleSummaryResponse(
        UUID uuid,
        String name,
        String description,
        boolean system,
        boolean active,
        long permissionCount,
        long userCount,
        Instant updatedAt,
        String updatedBy
) {
    public static RoleSummaryResponse from(RoleSummary role) {
        return new RoleSummaryResponse(
                role.uuid(), role.name(), role.description(), role.system(), role.active(),
                role.permissionCount(), role.userCount(), role.updatedAt(), role.updatedBy()
        );
    }
}
