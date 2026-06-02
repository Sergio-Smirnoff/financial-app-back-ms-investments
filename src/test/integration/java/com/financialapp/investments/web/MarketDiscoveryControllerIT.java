package com.financialapp.investments.web;

import com.financialapp.investments.support.AbstractIntegrationIT;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MarketDiscoveryControllerIT extends AbstractIntegrationIT {

    private static final long USER = 40L;

    @Test
    void discovery_returnsTrendingQuotesNotOwnedByUser() throws Exception {
        seedMarketQuote("YPFD", "25000", "ARS", "3.5");
        seedMarketQuote("PAMP", "4500", "ARS", "-1.2");

        mockMvc.perform(authGet("/api/v1/investments/market/discovery")
                        .param("limit", "5")
                        .header("X-User-Id", USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}
