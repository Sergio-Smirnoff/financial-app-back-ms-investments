package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.model.fx.*;
import com.financialapp.investments.domain.usecase.fx.*;
import com.financialapp.investments.domain.usecase.fx.BackfillFxRates.BackfillResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FxRateController.class)
@TestPropertySource(properties = "INTERNAL_AUTH_TOKEN=test-token")
class FxRateControllerTest {

    private static final String TOKEN = "test-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetFxRates getFxRates;

    @MockBean
    private GetLatestFxRates getLatestFxRates;

    @MockBean
    private GetFxRatesAtDate getFxRatesAtDate;

    @MockBean
    private BackfillFxRates backfillFxRates;

    private FxRate sampleRate;

    @BeforeEach
    void setUp() {
        sampleRate = new FxRate(new FxRateId(1L), LocalDate.of(2026, 7, 28), FxView.MEP, new BigDecimal("1200.50"), new BigDecimal("1200.50"), FxRateSource.IOL_SYNTHETIC, null);
    }

    @Test
    void getRates_returnsListInEnvelope() throws Exception {
        when(getFxRates.execute(any())).thenReturn(List.of(sampleRate));

        mockMvc.perform(get("/api/v1/investments/fx/rates")
                        .header("X-Internal-Token", TOKEN)
                        .param("from", "2026-07-28")
                        .param("to", "2026-07-28")
                        .param("view", "MEP")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].view").value("MEP"))
                .andExpect(jsonPath("$.data[0].buy").value("1200.50"));
    }

    @Test
    void getLatestRates_returnsListInEnvelope() throws Exception {
        when(getLatestFxRates.execute()).thenReturn(List.of(sampleRate));

        mockMvc.perform(get("/api/v1/investments/fx/rates/latest")
                        .header("X-Internal-Token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].view").value("MEP"));
    }

    @Test
    void getRatesAtDate_returnsSnapshotInEnvelope() throws Exception {
        FxSnapshotRates snapshot = new FxSnapshotRates(new BigDecimal("1200.50"), null, new BigDecimal("950.00"), LocalDate.of(2026, 7, 28));
        when(getFxRatesAtDate.execute(any())).thenReturn(snapshot);

        mockMvc.perform(get("/api/v1/investments/fx/rates/at")
                        .header("X-Internal-Token", TOKEN)
                        .param("date", "2026-07-28")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.mepRate").value("1200.50"))
                .andExpect(jsonPath("$.data.cclRate").doesNotExist())
                .andExpect(jsonPath("$.data.oficialRate").value("950.00"))
                .andExpect(jsonPath("$.data.rateDate").value("2026-07-28"));
    }

    @Test
    void backfill_returnsCreatedCountInEnvelope() throws Exception {
        when(backfillFxRates.execute(any())).thenReturn(new BackfillResult(5));

        mockMvc.perform(post("/api/v1/investments/fx/rates/backfill")
                        .header("X-Internal-Token", TOKEN)
                        .param("from", "2026-07-01")
                        .param("to", "2026-07-28")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.createdCount").value(5));
    }
}
