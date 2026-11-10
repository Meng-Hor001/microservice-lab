package kh.edu.istad.platform.iam.application.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AssignRolesToUserCommand(
        UUID userUuid,
        Set<UUID> roleUuids,
        Instant expiresAt,
        String reason
) {
    public AssignRolesToUserCommand {
        roleUuids = roleUuids == null ? Set.of() : Set.copyOf(roleUuids);
    }
}
