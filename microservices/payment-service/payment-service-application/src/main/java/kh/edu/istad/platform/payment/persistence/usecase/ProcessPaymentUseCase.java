package kh.edu.istad.platform.payment.persistence.usecase;

import java.time.Clock;
import java.time.ZonedDateTime;
import kh.edu.istad.platform.payment.domain.entity.*;
import kh.edu.istad.platform.payment.domain.service.*;
/** Application boundary for the assignment's payment domain; no transport is prescribed. */
public class ProcessPaymentUseCase {
    private final PaymentDomainService service;
    private final Clock clock;

    public ProcessPaymentUseCase(PaymentDomainService service, Clock clock) {
        this.service = service; this.clock = clock;
    }
    public PaymentOutcome execute(Payment payment, CreditEntry credit) {
        return service.process(payment, credit, ZonedDateTime.now(clock));
    }
    public PaymentOutcome cancel(Payment payment, CreditEntry credit) {
        return service.cancel(payment, credit, ZonedDateTime.now(clock));
    }
}
