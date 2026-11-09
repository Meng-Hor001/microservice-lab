package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.UserAuthoritiesResult;
import java.util.UUID;

public record RoleReferenceResponse(UUID uuid, String name) {
    public static RoleReferenceResponse from(UserAuthoritiesResult.RoleAuthority role) {
        return new RoleReferenceResponse(role.uuid(), role.name());
    }
}
