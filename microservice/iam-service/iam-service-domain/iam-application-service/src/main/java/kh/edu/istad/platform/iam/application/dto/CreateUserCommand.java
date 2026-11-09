package kh.edu.istad.platform.iam.application.dto;

import kh.edu.istad.platform.iam.domain.valueobject.UserProfile;

import java.util.Set;
import java.util.UUID;

public record CreateUserCommand(
        String username,
        String password,
        UserProfile profile,
        Set<UUID> roleUuids
) {
    public CreateUserCommand {
        roleUuids = roleUuids == null
                ? Set.of()
                : Set.copyOf(roleUuids);
    }
}
