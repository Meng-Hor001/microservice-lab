package kh.edu.istad.platform.business.restapi.exception;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import kh.edu.istad.platform.business.domain.exception.DomainException;

@RestControllerAdvice
public class ApiExceptionHandler {


    private static final Logger log =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomain(DomainException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }



    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidation(
            WebExchangeBindException exception
    ) {
        var problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed"
        );

        var errors = exception.getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", String.valueOf(error.getDefaultMessage())
                ))
                .toList();

        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ProblemDetail handleMalformedBody(
            ServerWebInputException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON or invalid field type"
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(
            ResponseStatusException exception) {

        return ProblemDetail.forStatusAndDetail(
                exception.getStatusCode(),
                exception.getReason() == null
                        ? "Request failed"
                        : exception.getReason()
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception) {
        log.error("Unexpected business-service error", exception);

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Check the business-service logs."
        );
    }
}
