package kh.edu.istad.platform.iam.application.exception;

public final class DuplicateAssignmentException extends IllegalArgumentException {
    public DuplicateAssignmentException(String assignment) {
        super(assignment + " already exists; revoke it before assigning again");
    }
}
