package com.financialapp.investments.web.error;

import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.commons.web.error.ApiExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid input: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ApiResponse.failure(
                HttpStatus.BAD_REQUEST, "invalid_input", ex.getMessage(), null));
    }
}
