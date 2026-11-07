package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AssignPermissionsToRoleCommand(
        UUID roleUuid,
        Set<UUID> permissionUuids,
        Instant expiresAt,
        String reason
) {
    public AssignPermissionsToRoleCommand {
        permissionUuids = permissionUuids == null
                ? Set.of()
                : Set.copyOf(permissionUuids);
    }
}
