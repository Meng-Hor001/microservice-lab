package kh.edu.istad.platform.restapi.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @Size(max = 255)
        String streetAddress,

        @Size(max = 100)
        String locality,

        @Size(max = 100)
        String region,

        @Size(max = 20)
        String postalCode,

        @Pattern(
                regexp = "[A-Za-z]{2}",
                message = "must be a two-letter country code"
        )
        String country
) {
}
