package kh.edu.istad.platform.payment.domain.event;

import java.time.ZonedDateTime;
import java.util.List;
import kh.edu.istad.platform.payment.domain.entity.Payment;
public record PaymentFailedEvent(Payment payment, ZonedDateTime createdAt, List<String> failureMessages) implements PaymentEvent {
    public PaymentFailedEvent { failureMessages = List.copyOf(failureMessages); }
}
