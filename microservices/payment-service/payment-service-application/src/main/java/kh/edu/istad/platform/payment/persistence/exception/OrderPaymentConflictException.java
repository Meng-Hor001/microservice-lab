package kh.edu.istad.platform.payment.persistence.exception;

public class OrderPaymentConflictException extends RuntimeException {
    public OrderPaymentConflictException() {
        super("Order must exist, be PENDING, and match" + "the payment customer and amount");
    }
}
