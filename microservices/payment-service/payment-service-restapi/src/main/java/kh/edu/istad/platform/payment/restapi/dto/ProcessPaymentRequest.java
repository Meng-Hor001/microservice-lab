package kh.edu.istad.platform.payment.restapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentRequest(
        @NotNull
        UUID orderId,

        @NotNull
        UUID customerId,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @NotNull
        @DecimalMin("0.01")
        // Used as the opening balance only when this customer has no saved credit account.
        BigDecimal creditBalance
) {
}
