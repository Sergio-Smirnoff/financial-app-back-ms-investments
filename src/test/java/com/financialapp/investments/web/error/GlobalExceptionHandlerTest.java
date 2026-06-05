package com.financialapp.investments.web.error;

import com.financialapp.commons.core.response.ApiResponse;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDomain_returnsErrorStatusAndCode() {
        ResponseEntity<ApiResponse<Map<String, Object>>> r =
                handler.handleDomain(new ResourceNotFoundException("not here"));
        assertThat(r.getStatusCode().value()).isEqualTo(404);
        assertThat(r.getBody().getCode()).isEqualTo("resource_not_found");
        assertThat(r.getBody().getMessage()).isEqualTo("not here");
    }

    @Test
    void handleValidation_returns400_andFieldMap() throws Exception {
        Object target = new Object();
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(target, "obj");
        br.addError(new FieldError("obj", "name", "must not be blank"));
        Method m = getClass().getDeclaredMethod("dummy");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                new MethodParameter(m, -1), br);
        ResponseEntity<ApiResponse<Map<String, String>>> r = handler.handleValidation(ex);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().getCode()).isEqualTo("validation_error");
        assertThat(r.getBody().getData()).containsEntry("name", "must not be blank");
    }

    @Test
    void handleDataIntegrity_returnsConflictWithConstraintDetail() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("conflict");
        ResponseEntity<ApiResponse<Map<String, Object>>> r = handler.handleDataIntegrity(ex);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(r.getBody().getCode()).isEqualTo("database_conflict");
        assertThat(r.getBody().getData()).containsKey("constraint");
    }

    @Test
    void handleIllegalArgument_returnsInvalidInput() {
        ResponseEntity<ApiResponse<Void>> r = handler.handleIllegalArgument(
                new IllegalArgumentException("bad"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(r.getBody().getCode()).isEqualTo("invalid_input");
        assertThat(r.getBody().getMessage()).isEqualTo("bad");
    }

    @Test
    void handleGeneric_returns500() {
        ResponseEntity<ApiResponse<Void>> r = handler.handleGeneric(new RuntimeException("boom"));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(r.getBody().getCode()).isEqualTo("internal_error");
    }

    private void dummy() {}
}
