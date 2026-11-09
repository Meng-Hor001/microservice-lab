package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.RoleSummary;
import java.util.UUID;

public record RoleSummaryResponse(UUID uuid, String name, boolean active) {
    public static RoleSummaryResponse from(RoleSummary role) {
        return new RoleSummaryResponse(role.uuid(), role.name(), role.active());
    }
}
