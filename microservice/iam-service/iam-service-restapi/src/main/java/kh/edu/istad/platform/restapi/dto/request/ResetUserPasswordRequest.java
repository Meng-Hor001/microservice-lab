package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResetUserPasswordRequest(
        @NotBlank String newPassword
) {}
