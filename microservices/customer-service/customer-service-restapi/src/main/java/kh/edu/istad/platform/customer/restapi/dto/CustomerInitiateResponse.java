package kh.edu.istad.platform.customer.restapi.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerInitiateResponse(

        String username,
        String familyName,
        String giveName,
        String email,
        String phoneNumber
) {
}
