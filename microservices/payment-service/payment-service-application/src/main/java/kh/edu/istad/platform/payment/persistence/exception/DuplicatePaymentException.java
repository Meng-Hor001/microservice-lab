package kh.edu.istad.platform.payment.persistence.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException() {
        super("A payment already exists for this order");
    }
}
