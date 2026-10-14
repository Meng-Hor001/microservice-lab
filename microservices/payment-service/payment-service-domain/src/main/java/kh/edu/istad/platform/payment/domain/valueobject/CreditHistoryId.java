package kh.edu.istad.platform.payment.domain.valueobject;

import java.util.UUID;
import kh.edu.istad.platform.payment.domain.exception.DomainException;
public record CreditHistoryId(UUID value) {
    public CreditHistoryId { if (value == null) throw new DomainException("CreditHistoryId is required"); }
    public static CreditHistoryId generate() { return new CreditHistoryId(UUID.randomUUID()); }
}
