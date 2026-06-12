package com.financialapp.investments.web.mapper;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Cbu;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.usecase.holding.response.AccountValuationResult;
import com.financialapp.investments.domain.usecase.market.response.MarketOpportunityResult;
import com.financialapp.investments.domain.usecase.portfolio.response.AllocationBreakdownResult;
import com.financialapp.investments.domain.usecase.portfolio.response.CurrencyTotals;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioEvolutionPoint;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;
import com.financialapp.investments.web.dto.response.AccountValuationResponse;
import com.financialapp.investments.web.dto.response.HoldingDetailResponse;
import com.financialapp.investments.web.dto.response.HoldingResponse;
import com.financialapp.investments.web.dto.response.HoldingWithPriceResponse;
import com.financialapp.investments.web.dto.response.MarketDiscoveryResponse;
import com.financialapp.investments.web.dto.response.PortfolioEvolutionResponse;
import com.financialapp.investments.web.dto.response.PortfolioSummaryResponse;
import com.financialapp.investments.web.dto.response.PriceHistoryResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebMappersTest {

    private static final UserId USER = new UserId(1L);
    private static final BankNumber BANK = new BankNumber("007");
    private static final Cbu ACC = new Cbu("0070009000000000000099");
    private static final Ticker TIC = new Ticker("AAPL");
    private static final Money PRICE = Money.of(new BigDecimal("100.00"), "ARS");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    private final HoldingWebMapper holdingMapper = new HoldingWebMapper();
    private final PortfolioWebMapper portfolioMapper = new PortfolioWebMapper();

    private Holding holdingWith(HoldingId id, ThresholdConfig tc, NotificationTimestamps ts,
                                BankNumber bank, Cbu acc) {
        return new Holding(id, USER, bank, TIC, "Apple", AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("1.000000")), PRICE, tc, ts, NOW, NOW);
    }

    // -------- HoldingWebMapper --------

    @Test
    void holdingMapper_toResponse_full_serialisesBigDecimalsAsPlainStrings() {
        Holding h = holdingWith(new HoldingId(1L),
                new ThresholdConfig(new BigDecimal("10.50"), new BigDecimal("5.25")),
                NotificationTimestamps.empty(), BANK, ACC);
        HoldingResponse r = holdingMapper.toResponse(h);
        assertThat(r.getId()).isEqualTo(1L);
        assertThat(r.getUserId()).isEqualTo(1L);
        assertThat(r.getTicker()).isEqualTo("AAPL");
        assertThat(r.getCurrency()).isEqualTo("ARS");
        assertThat(r.getBankNumber()).isEqualTo("007");
        assertThat(r.getQuantity()).isEqualTo("1.000000");
        assertThat(r.getAvgPurchasePrice()).isEqualTo("100.00");
        assertThat(r.getNotifyGainThresholdPct()).isEqualTo("10.50");
        assertThat(r.getNotifyLossThresholdPct()).isEqualTo("5.25");
        assertThat(r.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void holdingMapper_toResponse_nullIdsAndThresholds() {
        Holding h = holdingWith(null, null, NotificationTimestamps.empty(), null, null);
        HoldingResponse r = holdingMapper.toResponse(h);
        assertThat(r.getId()).isNull();
        assertThat(r.getBankNumber()).isNull();
        assertThat(r.getNotifyGainThresholdPct()).isNull();
        assertThat(r.getNotifyLossThresholdPct()).isNull();
    }

    @Test
    void holdingMapper_toWithPriceResponse_full() {
        Holding h = holdingWith(new HoldingId(1L),
                new ThresholdConfig(new BigDecimal("10.00"), new BigDecimal("5.00")),
                NotificationTimestamps.empty().withGainNotifiedAt(NOW).withLossNotifiedAt(NOW),
                BANK, ACC);
        HoldingWithPriceResult result = new HoldingWithPriceResult(h,
                new BigDecimal("110.00"), new BigDecimal("110.00"),
                new BigDecimal("10.00"), new BigDecimal("10.00"));
        HoldingWithPriceResponse r = holdingMapper.toWithPriceResponse(result);
        assertThat(r.getCurrentPrice()).isEqualTo("110.00");
        assertThat(r.getPlPercent()).isEqualTo("10.00");
        assertThat(r.getLastGainNotifiedAt()).isEqualTo(NOW);
        assertThat(r.getLastLossNotifiedAt()).isEqualTo(NOW);
    }

    @Test
    void holdingMapper_toWithPriceResponse_nullThresholdsAndIds() {
        Holding h = holdingWith(null, null, NotificationTimestamps.empty(), null, null);
        HoldingWithPriceResponse r = holdingMapper.toWithPriceResponse(
                new HoldingWithPriceResult(h, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThat(r.getId()).isNull();
        assertThat(r.getNotifyGainThresholdPct()).isNull();
        assertThat(r.getCurrentPrice()).isEqualTo("1");
        assertThat(r.getPlAmount()).isEqualTo("0");
    }

    @Test
    void holdingMapper_toDetailResponse_fullAndNulls() {
        Holding h1 = holdingWith(new HoldingId(1L),
                new ThresholdConfig(new BigDecimal("10.00"), new BigDecimal("5.00")),
                NotificationTimestamps.empty().withGainNotifiedAt(NOW), BANK, ACC);
        HoldingDetailResponse r1 = holdingMapper.toDetailResponse(
                new HoldingWithPriceResult(h1, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThat(r1.getLastGainNotifiedAt()).isEqualTo(NOW);
        assertThat(r1.getNotifyLossThresholdPct()).isEqualTo("5.00");

        Holding h2 = holdingWith(null, null, NotificationTimestamps.empty(), null, null);
        HoldingDetailResponse r2 = holdingMapper.toDetailResponse(
                new HoldingWithPriceResult(h2, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO));
        assertThat(r2.getNotifyGainThresholdPct()).isNull();
        assertThat(r2.getBankNumber()).isNull();
    }

    @Test
    void holdingMapper_toValuationResponse() {
        AccountValuationResult r = new AccountValuationResult(BANK, new BigDecimal("500.00"), "USD", 3L);
        AccountValuationResponse resp = holdingMapper.toValuationResponse(r);
        assertThat(resp.getBankNumber()).isEqualTo("007");
        assertThat(resp.getTotalValuation()).isEqualTo("500.00");
        assertThat(resp.getCurrency()).isEqualTo("USD");
    }

    @Test
    void holdingMapper_toValuationResponse_nullTotal_remainsNull() {
        AccountValuationResult r = new AccountValuationResult(BANK, null, "USD", 0L);
        AccountValuationResponse resp = holdingMapper.toValuationResponse(r);
        assertThat(resp.getTotalValuation()).isNull();
    }

    // -------- PortfolioWebMapper --------

    @Test
    void portfolioMapper_toResponse_includesBreakdownAndNullBreakdown() {
        AllocationBreakdownResult brk = new AllocationBreakdownResult(AssetType.STOCK, PRICE, new BigDecimal("100.00"));
        CurrencyTotals withBrk = new CurrencyTotals(PRICE, PRICE, PRICE, new BigDecimal("0.00"), List.of(brk));
        CurrencyTotals empty = new CurrencyTotals(PRICE, PRICE, PRICE, new BigDecimal("0.00"), null);
        PortfolioSummaryResult result = new PortfolioSummaryResult(List.of(withBrk, empty));

        PortfolioSummaryResponse r = portfolioMapper.toResponse(result);
        assertThat(r.getByCurrency()).hasSize(2);
        assertThat(r.getByCurrency().get(0).getCurrency()).isEqualTo("ARS");
        assertThat(r.getByCurrency().get(0).getTotalValue()).isEqualTo("100.00");
        assertThat(r.getByCurrency().get(0).getPlPercent()).isEqualTo("0.00");
        assertThat(r.getByCurrency().get(0).getBreakdown()).hasSize(1);
        assertThat(r.getByCurrency().get(0).getBreakdown().get(0).getAssetType()).isEqualTo("STOCK");
        assertThat(r.getByCurrency().get(0).getBreakdown().get(0).getTotalValue()).isEqualTo("100.00");
        assertThat(r.getByCurrency().get(0).getBreakdown().get(0).getPercentage()).isEqualTo("100.00");
        assertThat(r.getByCurrency().get(1).getBreakdown()).isEmpty();
    }

    @Test
    void portfolioMapper_toEvolutionResponse() {
        PortfolioEvolutionPoint point = new PortfolioEvolutionPoint(LocalDate.of(2026, 1, 1),
                List.of(PRICE, Money.of(new BigDecimal("1.50"), "USD")));
        PortfolioEvolutionResponse r = portfolioMapper.toEvolutionResponse(point);
        assertThat(r.getDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(r.getTotals()).hasSize(2);
        assertThat(r.getTotals()).extracting(t -> t.getCurrency()).containsExactly("ARS", "USD");
        assertThat(r.getTotals().get(0).getTotalValue()).isEqualTo("100.00");
        assertThat(r.getTotals().get(1).getTotalValue()).isEqualTo("1.50");
    }

    // -------- PriceWebMapper --------

    @Test
    void priceMapper_toResponse_passesAllFields_asPlainStrings() {
        AssetPriceHistory h = new AssetPriceHistory(null, TIC, AssetType.STOCK,
                new BigDecimal("100.00"), new BigDecimal("90.00"), new BigDecimal("110.00"),
                new BigDecimal("80.00"), new BigDecimal("1000"), new BigDecimal("0.10"),
                "ARS", NOW);
        PriceHistoryResponse r = new PriceWebMapper().toResponse(h);
        assertThat(r.getTicker()).isEqualTo("AAPL");
        assertThat(r.getLastPrice()).isEqualTo("100.00");
        assertThat(r.getOpenPrice()).isEqualTo("90.00");
        assertThat(r.getHighPrice()).isEqualTo("110.00");
        assertThat(r.getLowPrice()).isEqualTo("80.00");
        assertThat(r.getVolume()).isEqualTo("1000");
        assertThat(r.getDailyVariation()).isEqualTo("0.10");
        assertThat(r.getCurrency()).isEqualTo("ARS");
        assertThat(r.getPricedAt()).isEqualTo(NOW);
    }

    @Test
    void priceMapper_avoidsScientificNotation_forSmallValues() {
        AssetPriceHistory h = new AssetPriceHistory(null, TIC, AssetType.STOCK,
                new BigDecimal("0.00000001"), null, null, null, null,
                new BigDecimal("1E-2"), "ARS", NOW);
        PriceHistoryResponse r = new PriceWebMapper().toResponse(h);
        assertThat(r.getLastPrice()).isEqualTo("0.00000001");
        assertThat(r.getDailyVariation()).isEqualTo("0.01");
        assertThat(r.getOpenPrice()).isNull();
    }

    // -------- MarketWebMapper --------

    @Test
    void marketMapper_toResponse_passesAllFields_asPlainStrings() {
        MarketOpportunityResult o = new MarketOpportunityResult(TIC,
                Money.of(new BigDecimal("50.00"), "USD"), new BigDecimal("5.00"), new BigDecimal("100"));
        MarketDiscoveryResponse r = new MarketWebMapper().toResponse(o);
        assertThat(r.getTicker()).isEqualTo("AAPL");
        assertThat(r.getPrice()).isEqualTo("50.00");
        assertThat(r.getCurrency()).isEqualTo("USD");
        assertThat(r.getVariation()).isEqualTo("5.00");
        assertThat(r.getVolume()).isEqualTo("100");
    }

    @Test
    void marketMapper_nullVariation_serialisedAsNull() {
        MarketOpportunityResult o = new MarketOpportunityResult(TIC,
                Money.of(new BigDecimal("50.00"), "USD"), null, null);
        MarketDiscoveryResponse r = new MarketWebMapper().toResponse(o);
        assertThat(r.getVariation()).isNull();
        assertThat(r.getVolume()).isNull();
    }
}
