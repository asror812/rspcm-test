package org.example.rspcm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ErrorMessageException.class)
    public ResponseEntity<?> handleErrorMessageException(ErrorMessageException ex, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getErrorCode().getStatusCode());
        return ResponseEntity.status(status)
                .body(
                        new ErrorMessage(new Timestamp(System.currentTimeMillis()), ex.getErrorCode().name(),
                                ex.getMessage(), request.getDescription(false)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.badRequest()
                .body(new ErrorMessage(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.BAD_REQUEST.name(),
                        fieldErrors.toString(),
                        request.getDescription(false)));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.NOT_FOUND.name(),
                        ex.getMessage(),
                        request.getDescription(false)
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorMessage(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.FORBIDDEN.name(),
                        ex.getMessage(),
                        request.getDescription(false)));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorMessage(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.NOT_FOUND.name(),
                        ex.getMessage(),
                        request.getDescription(false)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorMessage(
                        new Timestamp(System.currentTimeMillis()),
                        HttpStatus.INTERNAL_SERVER_ERROR.name(),
                        ex.getMessage(),
                        request.getDescription(false)));
    }


}
