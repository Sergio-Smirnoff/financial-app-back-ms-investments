package com.financialapp.investments.web;

import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.support.AbstractIntegrationIT;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PriceHistoryControllerIT extends AbstractIntegrationIT {

    // Seed >= 3 rows so the use case does not trigger an IOL backfill (BACKFILL_THRESHOLD = 3).
    private void seedThreePoints(String ticker) {
        seedHistory(ticker, AssetType.STOCK, "100", "ARS", LocalDateTime.of(2026, 1, 10, 10, 0));
        seedHistory(ticker, AssetType.STOCK, "105", "ARS", LocalDateTime.of(2026, 1, 11, 10, 0));
        seedHistory(ticker, AssetType.STOCK, "110", "ARS", LocalDateTime.of(2026, 1, 12, 10, 0));
    }

    @Test
    void history_withExplicitRange_returnsRows() throws Exception {
        seedThreePoints("GGAL");

        mockMvc.perform(authGet("/api/v1/investments/prices/history/GGAL")
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-02-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].ticker").value("GGAL"));
    }

    @Test
    void history_withoutRange_usesDefaultLookbackWindow() throws Exception {
        // No from/to -> controller falls back to a 10-year window ending now; seeded rows fall inside it.
        seedHistory("GGAL", AssetType.STOCK, "100", "ARS", LocalDateTime.now().minusDays(2));
        seedHistory("GGAL", AssetType.STOCK, "101", "ARS", LocalDateTime.now().minusDays(1));
        seedHistory("GGAL", AssetType.STOCK, "102", "ARS", LocalDateTime.now().minusHours(1));

        mockMvc.perform(authGet("/api/v1/investments/prices/history/GGAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }
}
