package kh.edu.istad.platform.payment.domain.valueobject;

import java.util.UUID;
import kh.edu.istad.platform.payment.domain.exception.DomainException;
public record PaymentId(UUID value) {
    public PaymentId { if (value == null) throw new DomainException("PaymentId is required"); }
    public static PaymentId generate() { return new PaymentId(UUID.randomUUID()); }
}
