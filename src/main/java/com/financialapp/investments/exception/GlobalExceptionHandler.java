package com.financialapp.investments.exception;

import com.financialapp.investments.model.dto.response.ApiResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeignException(FeignException ex) {
        String body = ex.contentUTF8();
        log.warn("Feign call failed: status={}, body={}", ex.status(), body);
        
        String message = "Communication error between services";
        // Try to extract "message" from JSON if possible
        if (body != null && body.contains("\"message\":\"")) {
            try {
                int start = body.indexOf("\"message\":\"") + 11;
                int end = body.indexOf("\"", start);
                if (end > start) {
                    message = body.substring(start, end);
                }
            } catch (Exception e) {
                log.error("Failed to parse Feign error body", e);
            }
        } else if (body != null && !body.isBlank() && !body.startsWith("{")) {
            // If it's just a plain text error message
            message = body;
        }
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex.status() >= 100 && ex.status() < 600) {
            status = HttpStatus.valueOf(ex.status());
        }
        
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred"));
    }
}
