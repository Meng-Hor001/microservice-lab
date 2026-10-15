package kh.edu.istad.platform.order.restapi.dto;

import kh.edu.istad.platform.order.domain.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        UUID trackingId,
        OrderStatus status,
        BigDecimal totalAmount
) {
}
