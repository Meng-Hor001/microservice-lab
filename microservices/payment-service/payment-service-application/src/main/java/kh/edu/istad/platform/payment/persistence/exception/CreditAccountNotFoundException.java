package kh.edu.istad.platform.payment.persistence.exception;

import java.util.UUID;

public class CreditAccountNotFoundException extends RuntimeException {
    public CreditAccountNotFoundException(UUID customerId) {
        super("Credit account not found for customer :" + customerId);
    }
}
