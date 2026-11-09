package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record   UserAuthoritiesResult (
        List<RoleAuthority> roles,
        List<DirectPermissionAuthority> directPermissions,
        List<String> effectivePermissions
){
    public UserAuthoritiesResult {
        roles = roles == null ? List.of() : List.copyOf(roles);
        directPermissions = directPermissions == null
                ? List.of()
                : List.copyOf(directPermissions);
        effectivePermissions = effectivePermissions == null
                ? List.of()
                : List.copyOf(effectivePermissions);
    }

    public record RoleAuthority(UUID uuid, String name, Instant expiresAt) {}

    public record DirectPermissionAuthority(String name, String effect) {}
}
