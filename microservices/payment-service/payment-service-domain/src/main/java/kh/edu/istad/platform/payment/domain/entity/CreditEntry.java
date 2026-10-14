package kh.edu.istad.platform.payment.domain.entity;

import kh.edu.istad.platform.payment.domain.valueobject.*;
import kh.edu.istad.platform.payment.domain.exception.DomainException;
public record CreditEntry(CreditEntryId id, CustomerId customerId, Money totalCreditAmount) {
    public CreditEntry {
        if (id == null || customerId == null || totalCreditAmount == null) throw new DomainException("Credit account is required");
    }
    public CreditEntry addCreditAmount(Money amount) {
        amount.requirePositive();
        return new CreditEntry(id, customerId, totalCreditAmount.add(amount));
    }
    public CreditEntry subtractCreditAmount(Money amount) {
        amount.requirePositive();
        if (!totalCreditAmount.covers(amount)) throw new DomainException("Insufficient credit");
        return new CreditEntry(id, customerId, totalCreditAmount.subtract(amount));
    }
}
