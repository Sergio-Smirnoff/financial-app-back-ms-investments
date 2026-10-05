package com.financialapp.investments.application.holding;

import com.financialapp.investments.domain.common.model.BankNumber;

import com.financialapp.investments.application.holding.impl.GetHoldingDetailUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.service.BrokerFeeNetting;
import com.financialapp.investments.domain.usecase.holding.command.GetHoldingDetailCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetHoldingDetailUseCaseImplTest {

    @Mock
    private HoldingRepository holdingRepository;
    @Mock
    private AssetPriceRepository assetPriceRepository;
    @Mock
    private BrokerFeeScheduleRepository brokerFeeScheduleRepository;

    private GetHoldingDetailUseCaseImpl useCase;

    private static final UserId USER_ID = new UserId(1L);

    @BeforeEach
    void setUp() {
        useCase = new GetHoldingDetailUseCaseImpl(holdingRepository, assetPriceRepository, brokerFeeScheduleRepository,
                new BrokerFeeNetting());
    }

    @Test
    void execute_computesPlCorrectly() {
        Holding holding = holding("AAPL", new BigDecimal("10"), new BigDecimal("100"));
        when(holdingRepository.findByIdAndUserId(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(new Ticker("AAPL")))
                .thenReturn(Optional.of(assetPrice("AAPL", new BigDecimal("120"))));

        HoldingWithPriceResult result = useCase.execute(
                new GetHoldingDetailCommand(USER_ID, holding.id()));

        assertThat(result.currentPrice()).isEqualByComparingTo(new BigDecimal("120"));
        assertThat(result.currentValue()).isEqualByComparingTo(new BigDecimal("1200"));
        assertThat(result.plAmount()).isEqualByComparingTo(new BigDecimal("200"));
        assertThat(result.plPercent()).isEqualByComparingTo(new BigDecimal("20.0000"));
    }

    @Test
    void execute_fallsBackToAvgPrice_whenNoMarketPrice() {
        Holding holding = holding("AAPL", new BigDecimal("5"), new BigDecimal("200"));
        when(holdingRepository.findByIdAndUserId(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(any(Ticker.class))).thenReturn(Optional.empty());

        HoldingWithPriceResult result = useCase.execute(
                new GetHoldingDetailCommand(USER_ID, holding.id()));

        assertThat(result.currentPrice()).isEqualByComparingTo(new BigDecimal("200"));
        assertThat(result.plAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.plPercent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void execute_throwsResourceNotFoundException_whenHoldingMissing() {
        when(holdingRepository.findByIdAndUserId(any(HoldingId.class), eq(USER_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.execute(new GetHoldingDetailCommand(USER_ID, new HoldingId(999L))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_throwsResourceNotFoundException_whenUserMismatch() {
        UserId otherUser = new UserId(999L);
        when(holdingRepository.findByIdAndUserId(any(HoldingId.class), eq(otherUser))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.execute(new GetHoldingDetailCommand(otherUser, new HoldingId(1L))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void execute_bondDetailIsValuedPerHundredNominal() {
        Holding ao29 = new Holding(new HoldingId(43L), USER_ID, new BankNumber("017"),
                new Ticker("AO29"), "Bono 2029", AssetType.BOND,
                new HoldingQuantity(new BigDecimal("687")), Money.of(new BigDecimal("1435.78"), "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
        when(holdingRepository.findByIdAndUserId(ao29.id(), USER_ID)).thenReturn(Optional.of(ao29));
        when(assetPriceRepository.findByTicker(new Ticker("AO29")))
                .thenReturn(Optional.of(assetPrice("AO29", new BigDecimal("131700"))));

        HoldingWithPriceResult result = useCase.execute(new GetHoldingDetailCommand(USER_ID, ao29.id()));

        assertThat(result.currentValue()).isEqualByComparingTo("904779.00");
        assertThat(result.plAmount()).isEqualByComparingTo("-81601.86");
    }

    private static Holding holding(String ticker, BigDecimal quantity, BigDecimal avgPrice) {
        return new Holding(new HoldingId(42L), USER_ID, new BankNumber("007"),
                new Ticker(ticker), "Test", AssetType.STOCK,
                new HoldingQuantity(quantity), Money.of(avgPrice, "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static AssetPrice assetPrice(String ticker, BigDecimal price) {
        return new AssetPrice(new AssetPriceId(1L), new Ticker(ticker), AssetType.STOCK,
                price, "ARS", null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
