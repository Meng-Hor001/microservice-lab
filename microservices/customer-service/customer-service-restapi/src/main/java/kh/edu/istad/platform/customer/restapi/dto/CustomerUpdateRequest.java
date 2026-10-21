package kh.edu.istad.platform.customer.restapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerUpdateRequest(

        @NotBlank
        String username,

        @NotBlank
        String familyName,

        @NotBlank
        String givenName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String phoneNumber,

        String segmentId
) {
}
