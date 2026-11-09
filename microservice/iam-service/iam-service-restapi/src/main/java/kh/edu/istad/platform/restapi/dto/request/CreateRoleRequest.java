package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.UUID;

public record CreateRoleRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 255) String description,
        Set<@NotNull UUID> permissionUuids
) {
    public CreateRoleRequest {
        permissionUuids = permissionUuids == null ? Set.of() : permissionUuids;
    }
}
