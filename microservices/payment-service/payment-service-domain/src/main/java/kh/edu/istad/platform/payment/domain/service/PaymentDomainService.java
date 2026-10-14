package kh.edu.istad.platform.payment.domain.service;

import java.time.ZonedDateTime;
import java.util.*;
import kh.edu.istad.platform.payment.domain.entity.*;
import kh.edu.istad.platform.payment.domain.valueobject.*;
import kh.edu.istad.platform.payment.domain.event.*;
import kh.edu.istad.platform.payment.domain.exception.DomainException;

/** Pure calculations. A persistence adapter must atomically store all returned changes. */
public class PaymentDomainService {
    public PaymentOutcome process(Payment payment, CreditEntry credit, ZonedDateTime now) {
        validateOwner(payment, credit);
        if (payment.status() != PaymentStatus.PENDING) throw new DomainException("Payment already processed");
        if (!credit.totalCreditAmount().covers(payment.price())) {
            Payment failed = payment.fail();
            return new PaymentOutcome(failed, credit, Optional.empty(),
                    new PaymentFailedEvent(failed, now, List.of("Insufficient credit")));
        }
        Payment completed = payment.complete();
        CreditEntry debited = credit.subtractCreditAmount(payment.price());
        var history = new CreditHistory(CreditHistoryId.generate(), payment.customerId(), payment.price(), TransactionType.DEBIT);
        return new PaymentOutcome(completed, debited, Optional.of(history), new PaymentCompletedEvent(completed, now, List.of()));
    }
    public PaymentOutcome cancel(Payment payment, CreditEntry credit, ZonedDateTime now) {
        validateOwner(payment, credit);
        Payment cancelled = payment.cancel();
        CreditEntry refunded = credit.addCreditAmount(payment.price());
        var history = new CreditHistory(CreditHistoryId.generate(), payment.customerId(), payment.price(), TransactionType.CREDIT);
        return new PaymentOutcome(cancelled, refunded, Optional.of(history), new PaymentCancelledEvent(cancelled, now, List.of()));
    }
    private void validateOwner(Payment payment, CreditEntry credit) {
        if (payment == null || credit == null || !payment.customerId().equals(credit.customerId()))
            throw new DomainException("Credit account must belong to payment customer");
    }
}
