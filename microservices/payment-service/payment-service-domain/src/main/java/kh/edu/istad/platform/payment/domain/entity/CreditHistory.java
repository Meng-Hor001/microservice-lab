package kh.edu.istad.platform.payment.domain.entity;

import kh.edu.istad.platform.payment.domain.valueobject.*;
import kh.edu.istad.platform.payment.domain.exception.DomainException;
public record CreditHistory(
        CreditHistoryId id,
        CustomerId customerId,
        Money amount,
        TransactionType transactionType) {
    public CreditHistory {
        if (id == null || customerId == null || amount == null || transactionType == null) throw new DomainException("Invalid credit history");
        amount.requirePositive();
    }
}
