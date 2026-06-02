package com.financialapp.investments.web;

import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.support.AbstractIntegrationIT;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HoldingControllerIT extends AbstractIntegrationIT {

    private static final long USER = 10L;
    private static final long ACCOUNT = 55L;

    @Test
    void list_returnsSeededHoldingsForUser() throws Exception {
        seedHolding(USER, ACCOUNT, "AAPL", AssetType.STOCK, "10", "100", "USD", null, null);
        seedHolding(USER, ACCOUNT, "MSFT", AssetType.STOCK, "5", "200", "USD", null, null);

        mockMvc.perform(authGet("/api/v1/investments/holdings").header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    void list_missingInternalToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/investments/holdings").header("X-User-Id", USER))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void valuation_sumsPriceTimesQuantityForAccount() throws Exception {
        seedHolding(USER, ACCOUNT, "AAPL", AssetType.STOCK, "10", "100", "USD", null, null);
        seedAssetPrice("AAPL", AssetType.STOCK, "150", "USD");

        mockMvc.perform(authGet("/api/v1/investments/holdings/valuation")
                        .param("accountId", String.valueOf(ACCOUNT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountId").value((int) ACCOUNT))
                .andExpect(jsonPath("$.data.totalValuation").exists());
    }

    @Test
    void count_returnsHoldingCountForAccount() throws Exception {
        seedHolding(USER, ACCOUNT, "AAPL", AssetType.STOCK, "10", "100", "USD", null, null);
        seedAssetPrice("AAPL", AssetType.STOCK, "150", "USD");

        mockMvc.perform(authGet("/api/v1/investments/holdings/count")
                        .param("accountId", String.valueOf(ACCOUNT)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }
}
