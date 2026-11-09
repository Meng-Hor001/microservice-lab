package kh.edu.istad.platform.restapi.dto.response;

import kh.edu.istad.platform.iam.application.dto.UserAuthoritiesResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserAuthoritiesResponse(
        List<RoleAuthorityResponse> roles,
        List<DirectPermissionResponse> directPermissions,
        List<String> effectivePermissions
) {
    public UserAuthoritiesResponse {
        roles = List.copyOf(roles);
        directPermissions = List.copyOf(directPermissions);
        effectivePermissions = List.copyOf(effectivePermissions);
    }

    public static UserAuthoritiesResponse from(UserAuthoritiesResult authorities) {
        return new UserAuthoritiesResponse(
                authorities.roles().stream().map(role -> new RoleAuthorityResponse(
                        role.uuid(), role.name(), role.expiresAt()
                )).toList(),
                authorities.directPermissions().stream().map(permission -> new DirectPermissionResponse(
                        permission.name(), permission.effect()
                )).toList(),
                authorities.effectivePermissions()
        );
    }

    public record RoleAuthorityResponse(UUID uuid, String name, Instant expiresAt) {}
    public record DirectPermissionResponse(String name, String effect) {}
}
