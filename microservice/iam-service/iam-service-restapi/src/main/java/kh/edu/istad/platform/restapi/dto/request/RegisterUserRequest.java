package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        String password,

        @NotBlank
        String confirmPassword,

        @NotBlank
        @Size(max = 100)
        String givenName,

        @NotBlank
        @Size(max = 100)
        String familyName,

        @NotBlank
        @Pattern(regexp = "en|km")
        String locale
) {
}
