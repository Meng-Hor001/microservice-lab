package kh.edu.istad.common.domain.exception;

public class CustomerDomainException extends DomainException{

    public CustomerDomainException(String message) {
        super(message);
    }

    public CustomerDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
