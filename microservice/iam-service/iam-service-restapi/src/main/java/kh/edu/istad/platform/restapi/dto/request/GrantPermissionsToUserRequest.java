package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record GrantPermissionsToUserRequest(
        @NotEmpty Set<@NotNull UUID> permissionUuids,
        @NotNull Effect effect,
        @Future Instant expiresAt,
        @Size(max = 255) String reason
) {
    public enum Effect {
        GRANT,
        DENY
    }
}
