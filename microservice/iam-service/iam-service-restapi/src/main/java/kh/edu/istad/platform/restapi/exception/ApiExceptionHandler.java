package kh.edu.istad.platform.restapi.exception;

import kh.edu.istad.platform.iam.application.exception.*;
import kh.edu.istad.platform.iam.domain.exception.DomainRuleException;
import kh.edu.istad.platform.iam.domain.exception.DuplicateRoleException;
import kh.edu.istad.platform.restapi.dto.response.ValidationErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        var problem = problem(
                HttpStatus.BAD_REQUEST,
                "validation-error",
                "Validation failed",
                "One or more request fields are invalid"
        );

        var errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorResponse(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        problem.setProperty("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(
            ResourceNotFoundException exception
    ) {
        return problem(
                HttpStatus.NOT_FOUND,
                "resource-not-found",
                "Resource not found",
                exception.getMessage()
        );
    }

    @ExceptionHandler({
            DuplicateUserException.class,
            DuplicateRoleException.class,
            DuplicatePermissionException.class,
            DuplicateAssignmentException.class
    })
    public ProblemDetail handleDuplicate(RuntimeException exception) {
        return problem(
                HttpStatus.CONFLICT,
                "duplicate-resource",
                "Resource already exists",
                exception.getMessage()
        );
    }

    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class})
    public ProblemDetail handlePersistenceConflict(RuntimeException exception) {
        return problem(HttpStatus.CONFLICT, "persistence-conflict", "Resource conflict",
                "The operation conflicts with existing or concurrently updated data.");
    }

    @ExceptionHandler(VersionConflictException.class)
    public ProblemDetail handleVersionConflict(
            VersionConflictException exception
    ) {
        return problem(
                HttpStatus.CONFLICT,
                "version-conflict",
                "Version conflict",
                "The resource changed. Reload it before trying again."
        );
    }

    @ExceptionHandler({
            DomainRuleException.class,
            IllegalArgumentException.class
    })
    public ProblemDetail handleInvalidRequest(RuntimeException exception) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "invalid-request",
                "Invalid request",
                exception.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(
            AccessDeniedException exception
    ) {
        return problem(
                HttpStatus.FORBIDDEN,
                "access-denied",
                "Access denied",
                "You do not have permission to perform this operation."
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        log.error("Unexpected API failure", exception);

        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "internal-error",
                "Internal server error",
                "An unexpected error occurred."
        );
    }

    private ProblemDetail problem(
            HttpStatus status,
            String type,
            String title,
            String detail
    ) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("/problems/" + type));
        problem.setTitle(title);

        return problem;
    }
}
