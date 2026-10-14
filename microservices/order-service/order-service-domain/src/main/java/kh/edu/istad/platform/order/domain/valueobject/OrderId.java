package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;
import kh.edu.istad.platform.order.domain.exception.DomainException;
public record OrderId(UUID value) {
    public OrderId { if (value == null) throw new DomainException("OrderId is required"); }
    public static OrderId generate() { return new OrderId(UUID.randomUUID()); }
}
