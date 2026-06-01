package com.financialapp.investments.web.error;

import com.financialapp.investments.domain.exception.DomainException;
import com.financialapp.investments.web.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        log.warn("Domain error [{}]: {}", ex.error().code(), ex.getMessage());
        ErrorResponse body = ErrorResponse.builder()
                .status(ex.error().httpStatusCode())
                .code(ex.error().code())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(ex.error().httpStatusCode()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("validation_error")
                .message("Request validation failed")
                .details(Map.of("fields", fieldErrors))
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String constraintName = extractConstraintName(ex);
        log.warn("Data integrity violation [{}]: {}", constraintName, ex.getMessage());
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .code("database_conflict")
                .message("Data integrity violation")
                .details(constraintName != null ? Map.of("constraint", constraintName) : null)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid input: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code("invalid_input")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code("internal_error")
                .message("An unexpected error occurred")
                .build();
        return ResponseEntity.internalServerError().body(body);
    }

    private String extractConstraintName(DataIntegrityViolationException ex) {
        if (ex.getCause() instanceof ConstraintViolationException cve) {
            return cve.getConstraintName();
        }
        return null;
    }
}
