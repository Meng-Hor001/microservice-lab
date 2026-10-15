package kh.edu.istad.platform.payment.domain.entity;

import java.time.ZonedDateTime;
import kh.edu.istad.platform.payment.domain.valueobject.*;
import kh.edu.istad.platform.payment.domain.exception.DomainException;
public record Payment(
        PaymentId id,
        OrderId orderId,
        CustomerId customerId,
        Money price,
        PaymentStatus status,
        ZonedDateTime createdAt) {
    public Payment {
        if (id == null || orderId == null || customerId == null || price == null || status == null || createdAt == null)
            throw new DomainException("Payment data is required");
        price.requirePositive();
    }
    public static Payment initialize(OrderId order, CustomerId customer, Money price, ZonedDateTime now) {
        return new Payment(PaymentId.generate(), order, customer, price, PaymentStatus.PENDING, now);
    }
    public Payment complete() { return change(PaymentStatus.PENDING, PaymentStatus.COMPLETED); }
    public Payment fail() { return change(PaymentStatus.PENDING, PaymentStatus.FAILED); }
    public Payment cancel() { return change(PaymentStatus.COMPLETED, PaymentStatus.CANCELLED); }
    private Payment change(PaymentStatus expected, PaymentStatus next) {
        if (status != expected) throw new DomainException("Invalid payment transition: " + status + " to " + next);
        return new Payment(id, orderId, customerId, price, next, createdAt);
    }

    public void validatePayment(){
        if (id == null || orderId == null || customerId == null || price == null || status == null || createdAt == null) {
            throw new DomainException("Payment data is required");
        }
        price.requirePositive();
    }

    public static Payment initializePayment(OrderId orderId,
                                            CustomerId customerId,
                                            Money price,
                                            ZonedDateTime createdAt){
        return initialize(
                orderId, customerId, price, createdAt);
    }


    public Payment updateStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null){
            throw new DomainException("Payment status is required");
        }

        return switch (paymentStatus){
            case COMPLETED -> complete();
            case FAILED -> fail();
            case CANCELLED -> cancel();
            case PENDING -> throw new DomainException("cannot reset a payment to PENDING");
        };

    }
}
