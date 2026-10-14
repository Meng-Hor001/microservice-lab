package kh.edu.istad.platform.order.application.dto;

import java.util.UUID;

public record CreateOrderResult(
        UUID orderId,
        UUID trackingId
) {
}
