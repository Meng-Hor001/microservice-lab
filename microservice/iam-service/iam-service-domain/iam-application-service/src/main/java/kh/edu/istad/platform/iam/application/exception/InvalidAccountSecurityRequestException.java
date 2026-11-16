package kh.edu.istad.platform.iam.application.exception;

public class InvalidAccountSecurityRequestException extends RuntimeException {
    public InvalidAccountSecurityRequestException(String message) {
        super(message);
    }
}
