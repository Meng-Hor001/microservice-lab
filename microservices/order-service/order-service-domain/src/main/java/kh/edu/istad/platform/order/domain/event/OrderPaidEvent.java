package kh.edu.istad.platform.order.domain.event;

import java.time.ZonedDateTime;
import kh.edu.istad.platform.order.domain.entity.Order;
public record OrderPaidEvent(Order order, ZonedDateTime createdAt) {}
