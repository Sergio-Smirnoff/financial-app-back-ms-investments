package com.financialapp.investments.application.portfolio;

import com.financialapp.investments.domain.common.model.BankNumber;

import com.financialapp.investments.application.portfolio.impl.GetPortfolioSummaryUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.usecase.portfolio.GetHoldingsWithPricesUseCase;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioSummaryCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.AllocationBreakdownResult;
import com.financialapp.investments.domain.usecase.portfolio.response.CurrencyTotals;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPortfolioSummaryUseCaseImplTest {

    @Mock
    private GetHoldingsWithPricesUseCase getHoldingsWithPricesUseCase;

    @InjectMocks
    private GetPortfolioSummaryUseCaseImpl useCase;

    private static final UserId USER_ID = new UserId(1L);
    private static final Currency ARS = Currency.getInstance("ARS");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency BRL = Currency.getInstance("BRL");

    @Test
    void emptyPortfolio_returnsEmptyByCurrency() {
        when(getHoldingsWithPricesUseCase.execute(any())).thenReturn(List.of());

        PortfolioSummaryResult result = useCase.execute(new GetPortfolioSummaryCommand(USER_ID));

        assertThat(result.byCurrency()).isEmpty();
    }

    @Test
    void singleCurrency_returnsOneBucket() {
        HoldingWithPriceResult h = holdingResult("AAPL", ARS,
                new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("120"));
        when(getHoldingsWithPricesUseCase.execute(any())).thenReturn(List.of(h));

        PortfolioSummaryResult result = useCase.execute(new GetPortfolioSummaryCommand(USER_ID));

        assertThat(result.byCurrency()).hasSize(1);
        CurrencyTotals ars = result.byCurrency().get(0);
        assertThat(ars.currency()).isEqualTo(ARS);
        assertThat(ars.totalValue().amount()).isEqualByComparingTo(new BigDecimal("1200")); // 10 * 120
        assertThat(ars.totalCost().amount()).isEqualByComparingTo(new BigDecimal("1000"));  // 10 * 100
        assertThat(ars.totalPl().amount()).isEqualByComparingTo(new BigDecimal("200"));
        assertThat(ars.plPercent()).isEqualByComparingTo(new BigDecimal("20.0000"));
    }

    @Test
    void multipleCurrencies_returnsBucketPerCurrency_sortedByIsoCode() {
        HoldingWithPriceResult arsHolding = holdingResult("YPF", ARS,
                new BigDecimal("5"), new BigDecimal("200"), new BigDecimal("210"));
        HoldingWithPriceResult usdHolding = holdingResult("AAPL", USD,
                new BigDecimal("2"), new BigDecimal("150"), new BigDecimal("180"));
        HoldingWithPriceResult brlHolding = holdingResult("PETR4", BRL,
                new BigDecimal("3"), new BigDecimal("40"), new BigDecimal("45"));

        when(getHoldingsWithPricesUseCase.execute(any()))
                .thenReturn(List.of(arsHolding, usdHolding, brlHolding));

        PortfolioSummaryResult result = useCase.execute(new GetPortfolioSummaryCommand(USER_ID));

        assertThat(result.byCurrency()).hasSize(3);
        assertThat(result.byCurrency())
                .extracting(t -> t.currency().getCurrencyCode())
                .containsExactly("ARS", "BRL", "USD");
    }

    @Test
    void multipleHoldingsSameCurrency_sumIntoBucket() {
        HoldingWithPriceResult a = holdingResult("A", ARS,
                new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("110"));
        HoldingWithPriceResult b = holdingResult("B", ARS,
                new BigDecimal("5"), new BigDecimal("200"), new BigDecimal("220"));

        when(getHoldingsWithPricesUseCase.execute(any())).thenReturn(List.of(a, b));

        PortfolioSummaryResult result = useCase.execute(new GetPortfolioSummaryCommand(USER_ID));

        assertThat(result.byCurrency()).hasSize(1);
        CurrencyTotals ars = result.byCurrency().get(0);
        // value = 10*110 + 5*220 = 1100 + 1100 = 2200
        assertThat(ars.totalValue().amount()).isEqualByComparingTo(new BigDecimal("2200"));
        // cost  = 10*100 + 5*200 = 1000 + 1000 = 2000
        assertThat(ars.totalCost().amount()).isEqualByComparingTo(new BigDecimal("2000"));
    }

    @Test
    void zeroCostCurrency_plPercentIsZero_noDivByZero() {
        HoldingWithPriceResult h = holdingResult("X", ARS,
                new BigDecimal("10"), BigDecimal.ZERO, new BigDecimal("50"));
        when(getHoldingsWithPricesUseCase.execute(any())).thenReturn(List.of(h));

        PortfolioSummaryResult result = useCase.execute(new GetPortfolioSummaryCommand(USER_ID));

        assertThat(result.byCurrency().get(0).plPercent())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void breakdownCarriesCostPlAndCountPerTypeInsideEachCurrency() {
        HoldingWithPriceResult stockA = holdingResult("GGAL", AssetType.STOCK, ARS,
                new BigDecimal("10"), new BigDecimal("100"), new BigDecimal("110"));
        HoldingWithPriceResult stockB = holdingResult("YPFD", AssetType.STOCK, ARS,
                new BigDecimal("5"), new BigDecimal("200"), new BigDecimal("220"));
        HoldingWithPriceResult bond = holdingResult("GD30", AssetType.BOND, ARS,
                new BigDecimal("100"), new BigDecimal("1"), new BigDecimal("1.5"));
        HoldingWithPriceResult cedear = holdingResult("AAPL", AssetType.CEDEAR, USD,
                new BigDecimal("2"), new BigDecimal("150"), new BigDecimal("180"));
        when(getHoldingsWithPricesUseCase.execute(any())).thenReturn(List.of(stockA, stockB, bond, cedear));

        PortfolioSummaryResult result = useCase.execute(new GetPortfolioSummaryCommand(USER_ID));

        Map<String, CurrencyTotals> byCurrency = result.byCurrency().stream()
                .collect(Collectors.toMap(t -> t.currency().getCurrencyCode(), Function.identity()));
        List<AllocationBreakdownResult> ars = byCurrency.get("ARS").breakdown();
        assertThat(ars).extracting(AllocationBreakdownResult::assetType)
                .containsExactly(AssetType.BOND, AssetType.STOCK);
        AllocationBreakdownResult bonds = ars.get(0);
        assertThat(bonds.totalValue().amount()).isEqualByComparingTo("150");
        assertThat(bonds.totalCost().amount()).isEqualByComparingTo("100");
        assertThat(bonds.totalPl().amount()).isEqualByComparingTo("50");
        assertThat(bonds.percentage()).isEqualByComparingTo("6.3800");
        assertThat(bonds.count()).isEqualTo(1);
        AllocationBreakdownResult stocks = ars.get(1);
        assertThat(stocks.totalValue().amount()).isEqualByComparingTo("2200");
        assertThat(stocks.totalCost().amount()).isEqualByComparingTo("2000");
        assertThat(stocks.totalPl().amount()).isEqualByComparingTo("200");
        assertThat(stocks.percentage()).isEqualByComparingTo("93.6200");
        assertThat(stocks.count()).isEqualTo(2);
        assertThat(stocks.totalValue().currency()).isEqualTo(ARS);

        AllocationBreakdownResult usdCedears = byCurrency.get("USD").breakdown().get(0);
        assertThat(usdCedears.assetType()).isEqualTo(AssetType.CEDEAR);
        assertThat(usdCedears.totalValue().amount()).isEqualByComparingTo("360");
        assertThat(usdCedears.totalCost().amount()).isEqualByComparingTo("300");
        assertThat(usdCedears.totalPl().amount()).isEqualByComparingTo("60");
        assertThat(usdCedears.totalPl().currency()).isEqualTo(USD);
        assertThat(usdCedears.percentage()).isEqualByComparingTo("100.0000");
        assertThat(usdCedears.count()).isEqualTo(1);
    }

    private static HoldingWithPriceResult holdingResult(
            String ticker, Currency currency, BigDecimal qty, BigDecimal avgPrice, BigDecimal price) {
        return holdingResult(ticker, AssetType.STOCK, currency, qty, avgPrice, price);
    }

    private static HoldingWithPriceResult holdingResult(String ticker, AssetType assetType, Currency currency,
                                                        BigDecimal qty, BigDecimal avgPrice, BigDecimal price) {
        Holding holding = new Holding(
                new HoldingId(1L),
                USER_ID,
                new BankNumber("007"),
                new Ticker(ticker),
                "Test " + ticker,
                assetType,
                new HoldingQuantity(qty),
                new Money(avgPrice, currency),
                ThresholdConfig.disabled(),
                NotificationTimestamps.empty(),
                LocalDateTime.now(),
                LocalDateTime.now());
        BigDecimal currentValue = price.multiply(qty);
        BigDecimal costBasis = avgPrice.multiply(qty);
        BigDecimal plAmount = currentValue.subtract(costBasis);
        BigDecimal plPercent = costBasis.compareTo(BigDecimal.ZERO) != 0
                ? plAmount.divide(costBasis, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        return new HoldingWithPriceResult(holding, price, currentValue, plAmount, plPercent);
    }
}
