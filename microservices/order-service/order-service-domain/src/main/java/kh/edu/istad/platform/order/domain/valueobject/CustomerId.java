package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;
import kh.edu.istad.platform.order.domain.exception.DomainException;
public record CustomerId(UUID value) {
    public CustomerId { if (value == null) throw new DomainException("CustomerId is required"); }
    public static CustomerId generate() { return new CustomerId(UUID.randomUUID()); }
}
