package com.financialapp.investments.infrastructure.gateway;

import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.infrastructure.persistence.entity.HoldingJpaEntity;
import com.financialapp.investments.support.AbstractWireMockIT;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the ms-banks Feign outbound path via WireMock: closing a holding with a destination
 * account credits the proceeds through ms-banks; the request must carry the internal-token header.
 */
class BanksBalanceAdjustIT extends AbstractWireMockIT {

    @Test
    void closeHolding_withDestinationAccount_callsBanksWithInternalToken() throws Exception {
        HoldingJpaEntity holding =
                seedHolding(70L, 88L, "AAPL", AssetType.STOCK, "5", "100", "USD", null, null);

        mockMvc.perform(delete("/api/v1/investments/holdings/" + holding.getId())
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-User-Id", 70L)
                        .param("destinationAccountId", "99"))
                .andExpect(status().isOk());

        wireMock.verify(postRequestedFor(
                        urlPathEqualTo("/api/v1/banks/accounts/99/balance/adjust"))
                .withHeader("X-Internal-Token", equalTo(INTERNAL_TOKEN)));
        assertThat(holdingRepository.findById(holding.getId())).isEmpty();
    }

    @Test
    void closeHolding_withoutDestinationAccount_doesNotCallBanks() throws Exception {
        HoldingJpaEntity holding =
                seedHolding(71L, 88L, "AAPL", AssetType.STOCK, "5", "100", "USD", null, null);

        mockMvc.perform(delete("/api/v1/investments/holdings/" + holding.getId())
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-User-Id", 71L))
                .andExpect(status().isOk());

        wireMock.verify(0, postRequestedFor(
                urlPathMatching("/api/v1/banks/accounts/.*/balance/adjust")));
        assertThat(holdingRepository.findById(holding.getId())).isEmpty();
    }
}
