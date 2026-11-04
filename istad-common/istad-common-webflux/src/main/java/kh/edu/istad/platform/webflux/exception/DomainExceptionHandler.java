package kh.edu.istad.platform.webflux.exception;

import kh.edu.istad.common.domain.exception.DomainException;
import kh.edu.istad.platform.webflux.dto.ErrorApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DomainExceptionHandler {
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorApiResponse> handle(DomainException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorApiResponse(
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        null
                ));
    }
}
