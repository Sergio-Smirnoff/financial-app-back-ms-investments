package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.usecase.snapshot.EnsurePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.command.EnsurePortfolioSnapshotCommand;
import com.financialapp.investments.domain.usecase.snapshot.response.EnsureSnapshotResult;
import com.financialapp.investments.web.mapper.PortfolioWebMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PortfolioSnapshotController.class)
@Import(PortfolioWebMapper.class)
@TestPropertySource(properties = "INTERNAL_AUTH_TOKEN=test-token")
class PortfolioSnapshotControllerTest {

    private static final String URL = "/api/v1/investments/portfolio/snapshot/ensure-today";
    private static final String TOKEN = "test-token";

    @Autowired private MockMvc mockMvc;
    @MockBean private EnsurePortfolioSnapshotUseCase ensurePortfolioSnapshotUseCase;

    @Test
    void ensureToday_newSnapshot_answersCreatedAndTheDate() throws Exception {
        when(ensurePortfolioSnapshotUseCase.execute(new EnsurePortfolioSnapshotCommand(new UserId(42L))))
                .thenReturn(new EnsureSnapshotResult(true, LocalDate.of(2026, 9, 22)));

        mockMvc.perform(post(URL).header("X-Internal-Token", TOKEN).header("X-User-Id", 42))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.created").value(true))
                .andExpect(jsonPath("$.data.date").value("2026-09-22"));
    }

    @Test
    void ensureToday_existingSnapshot_answersNotCreated() throws Exception {
        when(ensurePortfolioSnapshotUseCase.execute(new EnsurePortfolioSnapshotCommand(new UserId(43L))))
                .thenReturn(new EnsureSnapshotResult(false, LocalDate.of(2026, 9, 22)));

        mockMvc.perform(post(URL).header("X-Internal-Token", TOKEN).header("X-User-Id", 43))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(false));
    }

    @Test
    void ensureToday_withoutTheInternalToken_is401AndTouchesNothing() throws Exception {
        mockMvc.perform(post(URL).header("X-User-Id", 42))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(ensurePortfolioSnapshotUseCase);
    }
}
