package kh.edu.istad.platform.iam.application.exception;

public final class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String field, String value) {
        super("User " + field + " already exists: " + value);
    }
}
