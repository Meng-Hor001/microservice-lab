package kh.edu.istad.platform.payment.domain.service;

import java.util.Optional;
import kh.edu.istad.platform.payment.domain.entity.*;
import kh.edu.istad.platform.payment.domain.event.PaymentEvent;
public record PaymentOutcome(Payment payment, CreditEntry creditEntry, Optional<CreditHistory> history, PaymentEvent event) {}
