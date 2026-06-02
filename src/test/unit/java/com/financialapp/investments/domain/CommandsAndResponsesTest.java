package com.financialapp.investments.domain;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.PageRequest;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.BankId;
import com.financialapp.investments.domain.model.holding.BanksAccountId;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.model.holding.NotificationTimestamps;
import com.financialapp.investments.domain.model.holding.ThresholdConfig;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.usecase.holding.command.CloseHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.command.CreateHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.command.GetAccountValuationCommand;
import com.financialapp.investments.domain.usecase.holding.command.GetHoldingDetailCommand;
import com.financialapp.investments.domain.usecase.holding.command.ListHoldingsCommand;
import com.financialapp.investments.domain.usecase.holding.command.UpdateHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.response.AccountValuationResult;
import com.financialapp.investments.domain.usecase.market.command.GetMarketDiscoveryCommand;
import com.financialapp.investments.domain.usecase.market.response.MarketOpportunityResult;
import com.financialapp.investments.domain.usecase.portfolio.command.GetHoldingsWithPricesCommand;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioEvolutionCommand;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioSummaryCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.AllocationBreakdownResult;
import com.financialapp.investments.domain.usecase.portfolio.response.CurrencyTotals;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioEvolutionPoint;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;
import com.financialapp.investments.domain.usecase.price.command.GetPriceHistoryCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandsAndResponsesTest {

    private static final UserId USER = new UserId(1L);
    private static final HoldingId HID = new HoldingId(10L);
    private static final BanksAccountId ACC = new BanksAccountId(100L);
    private static final BankId BANK = new BankId(2L);
    private static final Ticker TIC = new Ticker("AAPL");
    private static final HoldingQuantity QTY = new HoldingQuantity(BigDecimal.ONE);
    private static final Money ARS = Money.of(new BigDecimal("100"), "ARS");
    private static final Money ARS_ZERO = Money.zero("ARS");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void holdingCommands_accessors() {
        CloseHoldingCommand c = new CloseHoldingCommand(USER, HID, ACC);
        assertThat(c.destinationAccountId()).isEqualTo(ACC);

        CreateHoldingCommand cr = new CreateHoldingCommand(USER, ACC, BANK, TIC, "n",
                AssetType.STOCK, QTY, ARS, ThresholdConfig.disabled(), ACC);
        assertThat(cr.ticker()).isEqualTo(TIC);
        assertThat(cr.fundingAccountId()).isEqualTo(ACC);

        UpdateHoldingCommand up = new UpdateHoldingCommand(USER, HID, ACC, BANK, TIC, "n",
                AssetType.STOCK, QTY, ARS, ThresholdConfig.disabled(), ACC);
        assertThat(up.newQuantity()).isEqualTo(QTY);

        GetAccountValuationCommand g = new GetAccountValuationCommand(ACC);
        assertThat(g.accountId()).isEqualTo(ACC);

        GetHoldingDetailCommand d = new GetHoldingDetailCommand(USER, HID);
        assertThat(d.holdingId()).isEqualTo(HID);

        ListHoldingsCommand l = new ListHoldingsCommand(USER, AssetType.STOCK, new PageRequest(0, 10));
        assertThat(l.assetType()).isEqualTo(AssetType.STOCK);
    }

    @Test
    void portfolioCommands_accessors() {
        assertThat(new GetHoldingsWithPricesCommand(USER).userId()).isEqualTo(USER);
        assertThat(new GetPortfolioSummaryCommand(USER).userId()).isEqualTo(USER);
        GetPortfolioEvolutionCommand e = new GetPortfolioEvolutionCommand(USER, 30);
        assertThat(e.days()).isEqualTo(30);
    }

    @Test
    void marketCommands_accessors() {
        GetMarketDiscoveryCommand m = new GetMarketDiscoveryCommand(USER, 5);
        assertThat(m.limit()).isEqualTo(5);
    }

    @Test
    void priceCommands_accessors() {
        GetPriceHistoryCommand c = new GetPriceHistoryCommand(TIC, NOW, NOW.plusDays(1));
        assertThat(c.ticker()).isEqualTo(TIC);
        assertThat(c.to()).isAfter(c.from());
    }

    @Test
    void accountValuationResult_accessors() {
        AccountValuationResult r = new AccountValuationResult(ACC, BigDecimal.TEN, "ARS", 3L);
        assertThat(r.totalValuation()).isEqualByComparingTo("10");
        assertThat(r.currency()).isEqualTo("ARS");
        assertThat(r.holdingCount()).isEqualTo(3L);
    }

    @Test
    void marketOpportunityResult_accessors() {
        MarketOpportunityResult r = new MarketOpportunityResult(TIC, ARS, BigDecimal.ONE, BigDecimal.TEN);
        assertThat(r.ticker()).isEqualTo(TIC);
        assertThat(r.price()).isEqualTo(ARS);
    }

    @Test
    void holdingWithPriceResult_accessors() {
        Holding h = new Holding(HID, USER, ACC, BANK, TIC, "n", AssetType.STOCK, QTY, ARS,
                ThresholdConfig.disabled(), NotificationTimestamps.empty(), NOW, NOW);
        HoldingWithPriceResult r = new HoldingWithPriceResult(h, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);
        assertThat(r.holding()).isEqualTo(h);
        assertThat(r.currentPrice()).isEqualByComparingTo("1");
    }

    @Test
    void allocationBreakdownResult_nullChecks() {
        assertThatThrownBy(() -> new AllocationBreakdownResult(null, ARS, BigDecimal.ZERO))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new AllocationBreakdownResult(AssetType.STOCK, null, BigDecimal.ZERO))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new AllocationBreakdownResult(AssetType.STOCK, ARS, null))
                .isInstanceOf(NullPointerException.class);
        AllocationBreakdownResult ok = new AllocationBreakdownResult(AssetType.STOCK, ARS, BigDecimal.ONE);
        assertThat(ok.assetType()).isEqualTo(AssetType.STOCK);
    }

    @Test
    void currencyTotals_nullChecks() {
        assertThatThrownBy(() -> new CurrencyTotals(null, ARS, ARS, BigDecimal.ZERO, List.of()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CurrencyTotals(ARS, null, ARS, BigDecimal.ZERO, List.of()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CurrencyTotals(ARS, ARS, null, BigDecimal.ZERO, List.of()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CurrencyTotals(ARS, ARS, ARS, null, List.of()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void currencyTotals_nullBreakdown_becomesEmpty() {
        CurrencyTotals t = new CurrencyTotals(ARS, ARS, ARS, BigDecimal.ZERO, null);
        assertThat(t.breakdown()).isEmpty();
    }

    @Test
    void currencyTotals_currencyAccessor_returnsTotalValueCurrency() {
        CurrencyTotals t = new CurrencyTotals(ARS, ARS, ARS, BigDecimal.ZERO, List.of());
        assertThat(t.currency().getCurrencyCode()).isEqualTo("ARS");
    }

    @Test
    void currencyTotals_mixedCurrency_throws() {
        Money usd = Money.of(BigDecimal.ONE, "USD");
        assertThatThrownBy(() -> new CurrencyTotals(ARS, usd, ARS, BigDecimal.ZERO, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CurrencyTotals(ARS, ARS, usd, BigDecimal.ZERO, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void portfolioEvolutionPoint_validAndNulls() {
        PortfolioEvolutionPoint p = new PortfolioEvolutionPoint(LocalDate.of(2026, 1, 1), List.of(ARS));
        assertThat(p.totals()).containsExactly(ARS);
        assertThatThrownBy(() -> p.totals().add(ARS_ZERO)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> new PortfolioEvolutionPoint(null, List.of(ARS)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new PortfolioEvolutionPoint(LocalDate.now(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void portfolioSummaryResult_nullByCurrency_throws() {
        assertThatThrownBy(() -> new PortfolioSummaryResult(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void portfolioSummaryResult_copiesByCurrency() {
        CurrencyTotals t = new CurrencyTotals(ARS, ARS, ARS, BigDecimal.ZERO, List.of());
        PortfolioSummaryResult r = new PortfolioSummaryResult(List.of(t));
        assertThat(r.byCurrency()).containsExactly(t);
        assertThatThrownBy(() -> r.byCurrency().add(t)).isInstanceOf(UnsupportedOperationException.class);
    }
}
