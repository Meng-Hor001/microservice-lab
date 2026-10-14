package kh.edu.istad.platform.payment.domain.event;

import java.time.ZonedDateTime;
import java.util.List;
import kh.edu.istad.platform.payment.domain.entity.Payment;
public record PaymentCancelledEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMessages) implements PaymentEvent {
    public PaymentCancelledEvent { failureMessages = List.copyOf(failureMessages); }
}
