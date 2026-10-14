package kh.edu.istad.platform.order.application.dto;

import kh.edu.istad.platform.order.domain.valueobject.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderResult(
        UUID orderId,
        UUID trackingId,
        OrderStatus status,
        BigDecimal totalAmount
) {
}
