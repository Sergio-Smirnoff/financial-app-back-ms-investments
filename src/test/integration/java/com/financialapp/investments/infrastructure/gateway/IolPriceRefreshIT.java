package com.financialapp.investments.infrastructure.gateway;

import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.infrastructure.persistence.entity.AssetPriceJpaEntity;
import com.financialapp.investments.support.AbstractWireMockIT;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real IOL outbound path (RestTemplate + auth + JSON decoding) via WireMock:
 * POST /prices/refresh -> RefreshPricesUseCase -> IolGateway -> IolApiClient -> WireMock.
 */
class IolPriceRefreshIT extends AbstractWireMockIT {

    @Test
    void refresh_persistsPriceFetchedFromIol() throws Exception {
        seedHolding(60L, 1L, "GGAL", AssetType.STOCK, "10", "100", "ARS", null, null);

        mockMvc.perform(post("/api/v1/investments/prices/refresh")
                        .header("X-Internal-Token", INTERNAL_TOKEN))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.success").value(true));

        Optional<AssetPriceJpaEntity> stored = assetPriceRepository.findByTicker("GGAL");
        assertThat(stored).isPresent();
        assertThat(stored.get().getLastPrice()).isEqualByComparingTo(new BigDecimal("120.50"));

        wireMock.verify(getRequestedFor(
                urlPathEqualTo("/api/v2/bCBA/Titulos/GGAL/CotizacionDetalle")));
    }

    @Test
    void refresh_tickerWithNoIolQuote_persistsNothing() throws Exception {
        // No stub matches /Titulos/NOPE/... -> WireMock 404 -> IolApiClient swallows it (empty branch).
        seedHolding(62L, 1L, "NOPE", AssetType.STOCK, "10", "100", "ARS", null, null);

        mockMvc.perform(post("/api/v1/investments/prices/refresh")
                        .header("X-Internal-Token", INTERNAL_TOKEN))
                .andExpect(status().isOk());

        assertThat(assetPriceRepository.findByTicker("NOPE")).isEmpty();
    }
}
