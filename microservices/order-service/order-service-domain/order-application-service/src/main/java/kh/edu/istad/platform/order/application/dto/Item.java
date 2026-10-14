package kh.edu.istad.platform.order.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record Item(
        UUID productId,
        String name,
        BigDecimal price,
        int quantity
) {
}
