package kh.edu.istad.platform.order.domain.event;

import java.time.ZonedDateTime;
import kh.edu.istad.platform.order.domain.entity.Order;
public record OrderCreatedEvent(Order order, ZonedDateTime createdAt) {}
