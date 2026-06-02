package com.financialapp.investments.application.portfolio;

import com.financialapp.investments.application.portfolio.impl.GetHoldingsWithPricesUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.usecase.portfolio.command.GetHoldingsWithPricesCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetHoldingsWithPricesUseCaseImplTest {

    @Mock private HoldingRepository holdingRepository;
    @Mock private AssetPriceRepository assetPriceRepository;
    @InjectMocks private GetHoldingsWithPricesUseCaseImpl useCase;

    private static final UserId USER = new UserId(1L);

    @Test
    void execute_marketPriceAvailable_computesPlAndPercent() {
        Holding h = holding("AAPL", new BigDecimal("10"), new BigDecimal("100"));
        when(holdingRepository.findByUserId(USER)).thenReturn(List.of(h));
        when(assetPriceRepository.findAllByTickerIn(any()))
                .thenReturn(List.of(assetPrice("AAPL", new BigDecimal("150"))));

        List<HoldingWithPriceResult> r = useCase.execute(new GetHoldingsWithPricesCommand(USER));

        assertThat(r).hasSize(1);
        HoldingWithPriceResult x = r.get(0);
        assertThat(x.currentPrice()).isEqualByComparingTo("150");
        assertThat(x.currentValue()).isEqualByComparingTo("1500");
        assertThat(x.plAmount()).isEqualByComparingTo("500");
        assertThat(x.plPercent()).isEqualByComparingTo("50.00");
    }

    @Test
    void execute_priceMissing_fallsBackToAvgPurchase_plIsZero() {
        Holding h = holding("AAPL", new BigDecimal("10"), new BigDecimal("100"));
        when(holdingRepository.findByUserId(USER)).thenReturn(List.of(h));
        when(assetPriceRepository.findAllByTickerIn(any())).thenReturn(List.of());

        List<HoldingWithPriceResult> r = useCase.execute(new GetHoldingsWithPricesCommand(USER));
        assertThat(r.get(0).currentPrice()).isEqualByComparingTo("100");
        assertThat(r.get(0).plAmount()).isEqualByComparingTo("0");
        assertThat(r.get(0).plPercent()).isEqualByComparingTo("0");
    }

    @Test
    void execute_zeroCostBasis_plPercentIsZero() {
        Holding h = new Holding(new HoldingId(1L), USER, new BanksAccountId(1L), new BankId(1L),
                new Ticker("AAPL"), "n", AssetType.STOCK,
                new HoldingQuantity(BigDecimal.ONE), Money.of(BigDecimal.ZERO, "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
        when(holdingRepository.findByUserId(USER)).thenReturn(List.of(h));
        when(assetPriceRepository.findAllByTickerIn(any()))
                .thenReturn(List.of(assetPrice("AAPL", new BigDecimal("5"))));

        List<HoldingWithPriceResult> r = useCase.execute(new GetHoldingsWithPricesCommand(USER));
        assertThat(r.get(0).plPercent()).isEqualByComparingTo("0");
    }

    private static Holding holding(String ticker, BigDecimal qty, BigDecimal price) {
        return new Holding(new HoldingId(1L), USER, new BanksAccountId(1L), new BankId(1L),
                new Ticker(ticker), "n", AssetType.STOCK,
                new HoldingQuantity(qty), Money.of(price, "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static AssetPrice assetPrice(String ticker, BigDecimal price) {
        return new AssetPrice(new AssetPriceId(1L), new Ticker(ticker), AssetType.STOCK,
                price, "ARS", null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
