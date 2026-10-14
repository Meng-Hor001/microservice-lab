package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;
import kh.edu.istad.platform.order.domain.exception.DomainException;
public record OrderItemId(UUID value) {
    public OrderItemId { if (value == null) throw new DomainException("OrderItemId is required"); }
    public static OrderItemId generate() { return new OrderItemId(UUID.randomUUID()); }
}
