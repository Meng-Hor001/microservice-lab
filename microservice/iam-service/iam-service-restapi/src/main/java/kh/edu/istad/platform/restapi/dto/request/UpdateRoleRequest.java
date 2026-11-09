package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.*;

public record UpdateRoleRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 255) String description,
        @NotNull @PositiveOrZero Long version
) {}
