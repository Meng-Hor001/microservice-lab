package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.enums.PermissionEffect;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GrantPermissionToUserCommand(
        UUID userId,
        List<UUID> permissionUuids,
        PermissionEffect effect,
        Instant expiredAt,
        String reason
) {
    public GrantPermissionToUserCommand{
        permissionUuids = permissionUuids == null
                ?List.of()
                : List.copyOf(permissionUuids);
    }
}
