package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AssignPermissionsToRoleRequest(
        @NotEmpty Set<@NotNull UUID> permissionUuids,
        @Future Instant expiresAt,
        @Size(max = 255) String reason
) {}
