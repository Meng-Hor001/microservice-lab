package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AssignRolesToUserRequest(
        @NotEmpty Set<@NotNull UUID> roleUuids,
        @Future Instant expiresAt,
        @Size(max = 255) String reason
) {}
