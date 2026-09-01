package com.financialapp.investments.infrastructure.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.infrastructure.config.FxPairsProperties;
import com.financialapp.investments.infrastructure.gateway.dto.IolHistoricalPricePoint;
import com.financialapp.investments.infrastructure.gateway.dto.IolPriceDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FxLegPriceGatewayImplTest {

    @Mock
    private IolApiClient iolApiClient;

    @Mock
    private RestTemplate restTemplate;

    private FxPairsProperties fxPairsProperties;
    private FxLegPriceGatewayImpl gateway;

    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate PAST_DATE = LocalDate.of(2026, 7, 20);

    @BeforeEach
    void setUp() {
        fxPairsProperties = new FxPairsProperties(
                new FxPairsProperties.Pairs("AL30", "AL30D", "AL30", "AL30C"),
                new FxPairsProperties.Oficial("http://bcra.test")
        );
        gateway = new FxLegPriceGatewayImpl(iolApiClient, fxPairsProperties, restTemplate);
    }

    @Test
    void priceOn_today_callsLiveGetPrice() {
        IolPriceDetail detail = new IolPriceDetail(new BigDecimal("68000.00"), null, null, null, null, null, "ARS");
        given(iolApiClient.getPrice("AL30", AssetType.BOND)).willReturn(Optional.of(detail));

        Optional<BigDecimal> result = gateway.priceOn("AL30", TODAY);

        assertThat(result).isPresent().contains(new BigDecimal("68000.00"));
    }

    @Test
    void priceOn_pastDate_callsHistoricalSeries() {
        IolPriceDetail detail = new IolPriceDetail(new BigDecimal("65000.00"), null, null, null, null, null, "ARS");
        IolHistoricalPricePoint point = new IolHistoricalPricePoint(LocalDateTime.of(2026, 7, 20, 17, 0), detail);
        given(iolApiClient.getHistoricalSeries("AL30", AssetType.BOND, PAST_DATE, PAST_DATE)).willReturn(List.of(point));

        Optional<BigDecimal> result = gateway.priceOn("AL30", PAST_DATE);

        assertThat(result).isPresent().contains(new BigDecimal("65000.00"));
    }

    @Test
    void priceOn_emptySeries_returnsEmpty() {
        given(iolApiClient.getHistoricalSeries("AL30", AssetType.BOND, PAST_DATE, PAST_DATE)).willReturn(List.of());

        Optional<BigDecimal> result = gateway.priceOn("AL30", PAST_DATE);

        assertThat(result).isEmpty();
    }

    @Test
    void priceOn_exceptionInClient_returnsEmpty() {
        given(iolApiClient.getPrice(any(), any())).willThrow(new RuntimeException("API down"));

        Optional<BigDecimal> result = gateway.priceOn("AL30", TODAY);

        assertThat(result).isEmpty();
    }

    @Test
    void oficialRateOn_parsesBcraJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        var jsonNode = mapper.readTree("{\"results\":[{\"valor\":\"965.50\"}]}");
        given(restTemplate.getForObject("http://bcra.test", com.fasterxml.jackson.databind.JsonNode.class))
                .willReturn(jsonNode);

        Optional<BigDecimal> result = gateway.oficialRateOn(PAST_DATE);

        assertThat(result).isPresent().contains(new BigDecimal("965.50"));
    }

    @Test
    void oficialRateOn_bcraError_returnsEmpty() {
        given(restTemplate.getForObject(eq("http://bcra.test"), any()))
                .willThrow(new RuntimeException("Connection failed"));

        Optional<BigDecimal> result = gateway.oficialRateOn(PAST_DATE);

        assertThat(result).isEmpty();
    }
}
