package kh.edu.istad.platform.payment.restapi.exception;

import kh.edu.istad.platform.payment.persistence.exception.DuplicatePaymentException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler {
    @ExceptionHandler(DuplicatePaymentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicatePayment(DuplicatePaymentException exception) {
        return exception.getMessage();
    }
}
