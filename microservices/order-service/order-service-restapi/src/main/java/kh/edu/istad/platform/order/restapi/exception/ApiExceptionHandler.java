package kh.edu.istad.platform.order.restapi.exception;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import kh.edu.istad.platform.order.domain.exception.DomainException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    @ExceptionHandler(DomainException.class)
    public ProblemDetail domain(DomainException ex) { return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream().map(error ->
                Map.of("field", error.getField(), "message", String.valueOf(error.getDefaultMessage()))).toList());
        return problem;
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail malformed(HttpMessageNotReadableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request body or invalid field type");
    }
}
