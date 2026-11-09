package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.*;

public record CreatePermissionRequest(
        @NotBlank @Size(max = 50) String resource,
        @NotBlank @Size(max = 50) String action,
        @Size(max = 255) String description
) {}
