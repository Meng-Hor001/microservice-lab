package kh.edu.istad.platform.order.persistence.entity;

import java.util.UUID;

public record OrderItemEntityId(
        Integer id,
        UUID orderId
) {
}
