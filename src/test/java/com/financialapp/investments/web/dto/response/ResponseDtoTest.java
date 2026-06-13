package com.financialapp.investments.web.dto.response;

import com.financialapp.investments.domain.exception.InfrastructureException;
import com.financialapp.investments.web.dto.response.TickerResearchResponse;
import com.financialapp.investments.web.dto.response.TickerSearchResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseDtoTest {

    @Test
    void simpleResponseDtos_buildAndExpose() {
        AccountValuationResponse a = AccountValuationResponse.builder()
                .bankNumber("007").totalValuation("10").currency("ARS").build();
        assertThat(a.getBankNumber()).isEqualTo("007");
        assertThat(a.getTotalValuation()).isEqualTo("10");
        assertThat(a.getCurrency()).isEqualTo("ARS");

        AllocationBreakdown b = AllocationBreakdown.builder()
                .assetType("STOCK").totalValue("1").percentage("10").build();
        assertThat(b.getAssetType()).isEqualTo("STOCK");
        assertThat(b.getTotalValue()).isEqualTo("1");
        assertThat(b.getPercentage()).isEqualTo("10");

        CurrencyTotalsByDay d = CurrencyTotalsByDay.builder()
                .currency("USD").totalValue("1.50").build();
        assertThat(d.getCurrency()).isEqualTo("USD");
        assertThat(d.getTotalValue()).isEqualTo("1.50");

        CurrencyTotalsResponse c = CurrencyTotalsResponse.builder()
                .currency("ARS").totalValue("1").totalCost("1")
                .totalPl("0").plPercent("0").breakdown(List.of()).build();
        assertThat(c.getBreakdown()).isEmpty();
        assertThat(c.getTotalValue()).isEqualTo("1");

        HoldingResponse h = HoldingResponse.builder().id(1L).ticker("X").quantity("10.5").build();
        assertThat(h.getTicker()).isEqualTo("X");
        assertThat(h.getQuantity()).isEqualTo("10.5");

        HoldingDetailResponse hd = HoldingDetailResponse.builder().id(1L).plPercent("5.25").build();
        assertThat(hd.getId()).isEqualTo(1L);
        assertThat(hd.getPlPercent()).isEqualTo("5.25");

        HoldingWithPriceResponse hw = HoldingWithPriceResponse.builder().id(1L).currentPrice("100").build();
        assertThat(hw.getId()).isEqualTo(1L);
        assertThat(hw.getCurrentPrice()).isEqualTo("100");

        MarketDiscoveryResponse m = MarketDiscoveryResponse.builder()
                .ticker("X").price("1").currency("ARS")
                .variation("1").volume("1").build();
        assertThat(m.getTicker()).isEqualTo("X");
        assertThat(m.getPrice()).isEqualTo("1");

        PortfolioEvolutionResponse e = PortfolioEvolutionResponse.builder()
                .date(LocalDate.now()).totals(List.of()).build();
        assertThat(e.getTotals()).isEmpty();

        PortfolioSummaryResponse s = PortfolioSummaryResponse.builder()
                .byCurrency(List.of()).build();
        assertThat(s.getByCurrency()).isEmpty();

        PriceHistoryResponse ph = PriceHistoryResponse.builder()
                .ticker("X").lastPrice("1.00").currency("ARS")
                .pricedAt(LocalDateTime.now()).build();
        assertThat(ph.getTicker()).isEqualTo("X");
        assertThat(ph.getLastPrice()).isEqualTo("1.00");
    }

    @Test
    void tickerSearchResponse_buildAndExpose() {
        TickerSearchResponse r = TickerSearchResponse.builder()
                .ticker("YPFD").price("1200.50").currency("ARS").variation("2.10").build();
        assertThat(r.getTicker()).isEqualTo("YPFD");
        assertThat(r.getPrice()).isEqualTo("1200.50");
        assertThat(r.getCurrency()).isEqualTo("ARS");
        assertThat(r.getVariation()).isEqualTo("2.10");
    }

    @Test
    void tickerResearchResponse_buildAndExpose() {
        TickerResearchResponse.Point p = TickerResearchResponse.Point.builder()
                .date("2026-01-01").price("100.00").build();
        TickerResearchResponse r = TickerResearchResponse.builder()
                .ticker("YPFD").currency("ARS").currentPrice("1200.50")
                .variation("2.10").series(List.of(p)).build();
        assertThat(r.getTicker()).isEqualTo("YPFD");
        assertThat(r.getCurrency()).isEqualTo("ARS");
        assertThat(r.getCurrentPrice()).isEqualTo("1200.50");
        assertThat(r.getVariation()).isEqualTo("2.10");
        assertThat(r.getSeries()).hasSize(1);
        assertThat(r.getSeries().get(0).getDate()).isEqualTo("2026-01-01");
        assertThat(r.getSeries().get(0).getPrice()).isEqualTo("100.00");
    }

    @Test
    void infrastructureException_constructors() {
        InfrastructureException e1 = new InfrastructureException("m");
        assertThat(e1.getMessage()).isEqualTo("m");
        assertThat(e1.getCause()).isNull();
        Throwable t = new RuntimeException("x");
        InfrastructureException e2 = new InfrastructureException("m", t);
        assertThat(e2.getCause()).isSameAs(t);
    }
}
