package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;
import kh.edu.istad.platform.order.domain.exception.DomainException;
public record ProductId(UUID value) {
    public ProductId { if (value == null) throw new DomainException("ProductId is required"); }
    public static ProductId generate() { return new ProductId(UUID.randomUUID()); }
}
