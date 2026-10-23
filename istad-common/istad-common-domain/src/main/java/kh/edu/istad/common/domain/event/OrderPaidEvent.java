package kh.edu.istad.common.domain.event;

import kh.edu.istad.platform.order.domain.entity.Order;

import java.time.ZonedDateTime;

public record OrderPaidEvent(
        Order order,
        ZonedDateTime createdAt
) implements DomainEvent<Order>{
}
