package com.financialapp.investments.infrastructure.gateway.impl;

import com.financialapp.investments.domain.model.history.HistoricalPricePoint;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.model.price.PriceDetail;
import com.financialapp.investments.domain.exception.IolServiceException;
import com.financialapp.investments.infrastructure.gateway.IolApiClient;
import com.financialapp.investments.infrastructure.gateway.dto.IolHistoricalPricePoint;
import com.financialapp.investments.infrastructure.gateway.dto.IolMarketQuote;
import com.financialapp.investments.infrastructure.gateway.dto.IolPriceDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IolGatewayImplTest {

    @Mock private IolApiClient apiClient;
    @InjectMocks private IolGatewayImpl gateway;

    private static final Ticker TIC = new Ticker("AAPL");
    private static final LocalDate D1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate D2 = LocalDate.of(2026, 1, 2);

    @Test
    void getPrice_present_mapsToDomain_withResolvedCurrency() {
        IolPriceDetail dto = new IolPriceDetail(new BigDecimal("100"), BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "USD");
        when(apiClient.getPrice("AAPL", AssetType.STOCK)).thenReturn(Optional.of(dto));
        Optional<PriceDetail> r = gateway.getPrice(TIC, AssetType.STOCK);
        assertThat(r).isPresent();
        assertThat(r.get().currency()).isEqualTo("USD");
        assertThat(r.get().lastPrice()).isEqualByComparingTo("100");
    }

    @Test
    void getPrice_empty_returnsEmpty() {
        when(apiClient.getPrice(any(), any())).thenReturn(Optional.empty());
        assertThat(gateway.getPrice(TIC, AssetType.STOCK)).isEmpty();
    }

    @Test
    void getPrice_failure_wrappedAsInfrastructure() {
        when(apiClient.getPrice(any(), any())).thenThrow(new RuntimeException("boom"));
        assertThatThrownBy(() -> gateway.getPrice(TIC, AssetType.STOCK))
                .isInstanceOf(IolServiceException.class);
    }

    @Test
    void getHistoricalSeries_mapsAllPoints() {
        IolPriceDetail d = new IolPriceDetail(new BigDecimal("10"), null, null, null, null, null, "USD");
        IolHistoricalPricePoint p = new IolHistoricalPricePoint(LocalDateTime.of(2026, 1, 1, 0, 0), d);
        when(apiClient.getHistoricalSeries("AAPL", AssetType.CEDEAR, D1, D2)).thenReturn(List.of(p));

        List<HistoricalPricePoint> r = gateway.getHistoricalSeries(TIC, AssetType.CEDEAR, D1, D2);
        assertThat(r).hasSize(1);
        assertThat(r.get(0).currency()).isEqualTo("USD");
        assertThat(r.get(0).lastPrice()).isEqualByComparingTo("10");
    }

    @Test
    void getHistoricalSeries_dropsNonPositiveLastPricePoints() {
        IolPriceDetail good = new IolPriceDetail(new BigDecimal("10"), null, null, null, null, null, "USD");
        IolPriceDetail zero = new IolPriceDetail(BigDecimal.ZERO, null, null, null, null, null, "USD");
        IolHistoricalPricePoint pGood = new IolHistoricalPricePoint(LocalDateTime.of(2026, 1, 1, 0, 0), good);
        IolHistoricalPricePoint pZero = new IolHistoricalPricePoint(LocalDateTime.of(2026, 1, 2, 0, 0), zero);
        when(apiClient.getHistoricalSeries("AAPL", AssetType.STOCK, D1, D2))
                .thenReturn(List.of(pGood, pZero));

        List<HistoricalPricePoint> r = gateway.getHistoricalSeries(TIC, AssetType.STOCK, D1, D2);

        assertThat(r).hasSize(1);
        assertThat(r.get(0).lastPrice()).isEqualByComparingTo("10");
    }

    @Test
    void getHistoricalSeries_failure_wrapped() {
        when(apiClient.getHistoricalSeries(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));
        assertThatThrownBy(() -> gateway.getHistoricalSeries(TIC, AssetType.STOCK, D1, D2))
                .isInstanceOf(IolServiceException.class);
    }

    @Test
    void getPanelQuotes_mapsAll_withNullPriceFallbackZero() {
        IolMarketQuote q1 = new IolMarketQuote("AAPL", new BigDecimal("100"), BigDecimal.ONE, "USD");
        IolMarketQuote q2 = new IolMarketQuote("GOOG", null, BigDecimal.ONE, "ARS");
        when(apiClient.getPanelQuotes("merval")).thenReturn(List.of(q1, q2));

        List<MarketQuote> r = gateway.getPanelQuotes("merval");
        assertThat(r).hasSize(2);
        assertThat(r.get(0).price().amount()).isEqualByComparingTo("100");
        assertThat(r.get(1).price().amount()).isEqualByComparingTo("0");
        assertThat(r.get(0).price().currency().getCurrencyCode()).isEqualTo("USD");
        assertThat(r.get(1).price().currency().getCurrencyCode()).isEqualTo("ARS");
    }

    @Test
    void getPanelQuotes_failure_wrapped() {
        when(apiClient.getPanelQuotes(any())).thenThrow(new RuntimeException("boom"));
        assertThatThrownBy(() -> gateway.getPanelQuotes("x"))
                .isInstanceOf(IolServiceException.class);
    }
}
