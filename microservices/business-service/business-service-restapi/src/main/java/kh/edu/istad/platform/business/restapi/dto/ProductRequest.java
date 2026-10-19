package kh.edu.istad.platform.business.restapi.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
        @NotNull
        UUID productId,
        @NotBlank
        String name,
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal price,
        @Min(1)
        int quantity
) {
}
