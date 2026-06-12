package com.financialapp.investments.application.holding;

import com.financialapp.investments.application.holding.impl.GetAccountValuationUseCaseImpl;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.usecase.holding.command.GetAccountValuationCommand;
import com.financialapp.investments.domain.usecase.holding.response.AccountValuationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAccountValuationUseCaseImplTest {

    @Mock private HoldingQueryGateway holdingQueryGateway;
    @Mock private AssetPriceRepository assetPriceRepository;
    @InjectMocks private GetAccountValuationUseCaseImpl useCase;

    private static final UserId USER = new UserId(1L);
    private static final BankNumber BANK = new BankNumber("007");
    private static final Currency ARS = Currency.getInstance("ARS");

    @Test
    void execute_noHoldings_returnsZero() {
        when(holdingQueryGateway.findByUserIdAndBankNumberAndCurrency(USER, BANK, ARS)).thenReturn(List.of());
        AccountValuationResult r = useCase.execute(new GetAccountValuationCommand(USER, BANK, ARS));
        assertThat(r.totalValuation()).isEqualByComparingTo("0");
        assertThat(r.currency()).isEqualTo("ARS");
        assertThat(r.holdingCount()).isZero();
        assertThat(r.bankNumber()).isEqualTo(BANK);
    }

    @Test
    void execute_computesValuation_usingMarketPrice() {
        Holding h1 = holding("AAPL", new BigDecimal("2"), new BigDecimal("100"), "ARS");
        Holding h2 = holding("GOOG", new BigDecimal("3"), new BigDecimal("50"), "ARS");
        when(holdingQueryGateway.findByUserIdAndBankNumberAndCurrency(USER, BANK, ARS))
                .thenReturn(List.of(h1, h2));
        when(assetPriceRepository.findAllByTickerIn(any())).thenReturn(List.of(
                assetPrice("AAPL", new BigDecimal("200"))));

        AccountValuationResult r = useCase.execute(new GetAccountValuationCommand(USER, BANK, ARS));

        // AAPL: 200 * 2 = 400, GOOG fallback to avg 50 * 3 = 150 → 550
        assertThat(r.totalValuation()).isEqualByComparingTo("550");
        assertThat(r.currency()).isEqualTo("ARS");
        assertThat(r.holdingCount()).isEqualTo(2L);
        assertThat(r.bankNumber()).isEqualTo(BANK);
    }

    private static Holding holding(String ticker, BigDecimal qty, BigDecimal price, String ccy) {
        return new Holding(new HoldingId(1L), USER, BANK,
                new Ticker(ticker), "n", AssetType.STOCK,
                new HoldingQuantity(qty), Money.of(price, ccy),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static AssetPrice assetPrice(String ticker, BigDecimal price) {
        return new AssetPrice(new AssetPriceId(1L), new Ticker(ticker), AssetType.STOCK,
                price, "ARS", null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
