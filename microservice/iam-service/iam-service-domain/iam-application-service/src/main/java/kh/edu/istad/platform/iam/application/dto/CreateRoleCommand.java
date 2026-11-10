package kh.edu.istad.platform.iam.application.dto;

import java.util.Set;
import java.util.UUID;

public record CreateRoleCommand(
        String name,
        String description,
        Set<UUID> permissionUuids
) {

    public CreateRoleCommand {
        permissionUuids = permissionUuids == null
                ? Set.of()
                : Set.copyOf(permissionUuids);
    }
}
