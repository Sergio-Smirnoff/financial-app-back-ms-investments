package com.financialapp.investments.web;

import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.infrastructure.persistence.entity.HoldingJpaEntity;
import com.financialapp.investments.support.AbstractIntegrationIT;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PortfolioControllerIT extends AbstractIntegrationIT {

    private static final long USER = 20L;

    @Test
    void summary_groupsTotalsByCurrency() throws Exception {
        seedHolding(USER, 1L, "AAPL", AssetType.STOCK, "10", "100", "USD", null, null);
        seedAssetPrice("AAPL", AssetType.STOCK, "120", "USD");

        mockMvc.perform(authGet("/api/v1/investments/portfolio/summary").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.byCurrency[0].currency").value("USD"));
    }

    @Test
    void holdingsWithPrices_enrichesWithCurrentPrice() throws Exception {
        seedHolding(USER, 1L, "AAPL", AssetType.STOCK, "10", "100", "USD", null, null);
        seedAssetPrice("AAPL", AssetType.STOCK, "120", "USD");

        mockMvc.perform(authGet("/api/v1/investments/portfolio/holdings").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$.data[0].currentPrice").exists())
                .andExpect(jsonPath("$.data[0].plAmount").exists());
    }

    @Test
    void holdingDetail_returnsSingleHolding() throws Exception {
        HoldingJpaEntity holding =
                seedHolding(USER, 1L, "AAPL", AssetType.STOCK, "10", "100", "USD", null, null);
        seedAssetPrice("AAPL", AssetType.STOCK, "120", "USD");

        mockMvc.perform(authGet("/api/v1/investments/portfolio/holdings/" + holding.getId())
                        .header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ticker").value("AAPL"));
    }

    @Test
    void holdingDetail_unknownId_returns404() throws Exception {
        mockMvc.perform(authGet("/api/v1/investments/portfolio/holdings/999999")
                        .header("X-User-Id", USER))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("resource_not_found"));
    }
}
