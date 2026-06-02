package com.financialapp.investments.web.error;

import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.web.dto.response.ErrorResponse;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDomain_returnsErrorStatusAndCode() {
        ResponseEntity<ErrorResponse> r = handler.handleDomain(new ResourceNotFoundException("not here"));
        assertThat(r.getStatusCode().value()).isEqualTo(404);
        assertThat(r.getBody().getCode()).isEqualTo("resource_not_found");
        assertThat(r.getBody().getMessage()).isEqualTo("not here");
    }

    @Test
    void handleValidation_returns400_andFieldErrors() throws Exception {
        Object target = new Object();
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(target, "obj");
        br.addError(new FieldError("obj", "name", "must not be blank"));
        java.lang.reflect.Method m = getClass().getDeclaredMethod("dummy");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(m, -1), br);
        ResponseEntity<ErrorResponse> r = handler.handleValidation(ex);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().getCode()).isEqualTo("validation_error");
        assertThat(r.getBody().getDetails()).containsKey("fields");
    }

    @Test
    void handleDataIntegrity_extractsConstraintName_returnsConflict() {
        ConstraintViolationException cve = new ConstraintViolationException("msg",
                new SQLException(), "uq_holding");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("conflict", cve);
        ResponseEntity<ErrorResponse> r = handler.handleDataIntegrity(ex);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(r.getBody().getDetails()).containsEntry("constraint", "uq_holding");
    }

    @Test
    void handleDataIntegrity_noConstraintName_detailsNull() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("conflict",
                new RuntimeException("not-constraint"));
        ResponseEntity<ErrorResponse> r = handler.handleDataIntegrity(ex);
        assertThat(r.getBody().getDetails()).isNull();
    }

    @Test
    void handleIllegalArgument_returnsInvalidInput() {
        ResponseEntity<ErrorResponse> r = handler.handleIllegalArgument(
                new IllegalArgumentException("bad"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().getCode()).isEqualTo("invalid_input");
        assertThat(r.getBody().getMessage()).isEqualTo("bad");
    }

    @Test
    void handleGeneric_returns500_genericMessage() {
        ResponseEntity<ErrorResponse> r = handler.handleGeneric(new RuntimeException("boom"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(r.getBody().getCode()).isEqualTo("internal_error");
        assertThat(r.getBody().getMessage()).doesNotContain("boom");
    }

    @SuppressWarnings("unused")
    private void dummy() {}
}
