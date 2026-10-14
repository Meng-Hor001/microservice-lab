package kh.edu.istad.platform.order.restapi.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record CreateOrderRequest(@NotNull UUID customerId, @NotNull UUID businessId,
        @NotNull @Valid Address deliveryAddress, @NotEmpty @Size(max=100) List<@NotNull @Valid Item> items) {
    public record Address(@NotBlank @Size(max=255) String street, @NotBlank @Size(max=30) String postalCode,
                          @NotBlank @Size(max=100) String city) {}
    public record Item(@NotNull UUID productId, @NotBlank @Size(max=255) String name,
                       @NotNull @DecimalMin("0.01") @Digits(integer=17, fraction=2) BigDecimal price,
                       @Min(1) @Max(100000) int quantity) {}
}
