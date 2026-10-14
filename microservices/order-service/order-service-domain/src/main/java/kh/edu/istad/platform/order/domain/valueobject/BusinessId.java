package kh.edu.istad.platform.order.domain.valueobject;

import java.util.UUID;
import kh.edu.istad.platform.order.domain.exception.DomainException;
public record BusinessId(UUID value) {
    public BusinessId { if (value == null) throw new DomainException("BusinessId is required"); }
    public static BusinessId generate() { return new BusinessId(UUID.randomUUID()); }
}
