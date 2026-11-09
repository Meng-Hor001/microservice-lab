package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.*;

public record UpdatePermissionRequest(
        @NotBlank @Size(max = 50) String resource,
        @NotBlank @Size(max = 50) String action,
        @Size(max = 255) String description,
        @NotNull @PositiveOrZero Long version
) {}
