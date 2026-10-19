package kh.edu.istad.platform.business.restapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kh.edu.istad.platform.business.domain.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ValidateOrderRequest(
        @NotNull
        UUID orderId,

        @NotNull
        OrderStatus orderStatus,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal totalAmount,
        @NotEmpty
        List<@NotNull @Valid ProductRequest> products
) {
}
