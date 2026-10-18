package kh.edu.istad.platform.payment.restapi.exception;

import kh.edu.istad.platform.payment.persistence.exception.CreditAccountNotFoundException;
import kh.edu.istad.platform.payment.persistence.exception.DuplicatePaymentException;
import kh.edu.istad.platform.payment.persistence.exception.OrderPaymentConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler {
    @ExceptionHandler(DuplicatePaymentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicatePaymentConflict(DuplicatePaymentException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(CreditAccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleCreditAccountNotFound(CreditAccountNotFoundException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler(OrderPaymentConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleOrderPaymentConflict(OrderPaymentConflictException exception) {
        return exception.getMessage();
    }
}
