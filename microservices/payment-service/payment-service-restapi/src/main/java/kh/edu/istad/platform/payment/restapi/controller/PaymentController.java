package kh.edu.istad.platform.payment.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.payment.domain.entity.Payment;
import kh.edu.istad.platform.payment.domain.valueobject.CustomerId;
import kh.edu.istad.platform.payment.domain.valueobject.Money;
import kh.edu.istad.platform.payment.domain.valueobject.OrderId;
import kh.edu.istad.platform.payment.persistence.PaymentPersistenceService;
import kh.edu.istad.platform.payment.restapi.dto.PaymentResponse;
import kh.edu.istad.platform.payment.restapi.dto.ProcessPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController
{
    private final PaymentPersistenceService paymentPersistenceService;
    private final Clock paymentClock;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse process(
            @Valid @RequestBody ProcessPaymentRequest request) {

        var customerId = new CustomerId(request.customerId());

        var payment = Payment.initialize(
                new OrderId(request.orderId()),
                customerId,
                new Money(request.amount()),
                ZonedDateTime.now(paymentClock)
        );

        var outcome = paymentPersistenceService.process(
                payment,
                payment.createdAt());

        return new PaymentResponse(
                outcome.payment().id().value(),
                outcome.payment().orderId().value(),
                outcome.payment().customerId().value(),
                outcome.payment().status().name(),
                outcome.payment().price().amount(),
                outcome.creditEntry().totalCreditAmount().amount(),
                outcome.event().failureMessages()
        );
    }
}
