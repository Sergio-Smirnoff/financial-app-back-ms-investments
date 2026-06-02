package com.financialapp.investments.web;

import com.financialapp.investments.support.AbstractIntegrationIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Drives the {@code GlobalExceptionHandler} through the real servlet stack (filter + dispatcher
 * + handler advice), asserting the structured {@code ErrorResponse} body it produces.
 */
class GlobalExceptionHandlerIT extends AbstractIntegrationIT {

    @Test
    void resourceNotFound_returnsStructuredErrorBody() throws Exception {
        mockMvc.perform(authGet("/api/v1/investments/portfolio/holdings/424242")
                        .header("X-User-Id", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("resource_not_found"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void invalidRequestBody_returns400ValidationError() throws Exception {
        // Empty body fails bean validation -> handler emits a 400 validation_error, not a 500.
        mockMvc.perform(post("/api/v1/investments/holdings")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-User-Id", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("validation_error"));
    }
}
