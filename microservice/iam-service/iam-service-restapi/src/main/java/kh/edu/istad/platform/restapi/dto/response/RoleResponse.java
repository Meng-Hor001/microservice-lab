package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.RoleResult;
import java.util.UUID;

public record RoleResponse(
        UUID uuid, String name, String description, boolean system, boolean active, long version
) {
    public static RoleResponse from(RoleResult role) {
        return new RoleResponse(role.uuid(), role.name(), role.description(), role.system(), role.active(), role.version());
    }
}
