package com.financialapp.investments.application.holding;

import com.financialapp.investments.application.holding.impl.CloseHoldingUseCaseImpl;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.HoldingClosedEvent;
import com.financialapp.investments.domain.exception.FinancesServiceException;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.gateway.FinancesGateway;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.usecase.holding.command.CloseHoldingCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseHoldingUseCaseImplTest {

    @Mock
    private HoldingRepository holdingRepository;
    @Mock
    private AssetPriceRepository assetPriceRepository;
    @Mock
    private FinancesGateway financesGateway;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository brokerFeeScheduleRepository;

    @InjectMocks
    private CloseHoldingUseCaseImpl useCase;

    private static final UserId USER_ID = new UserId(1L);
    private static final Cbu DESTINATION_CBU = new Cbu("0070009000000000000099");

    @Test
    void close_recordsSaleProceeds_andPublishesEvent() {
        Holding holding = holding("AAPL", new BigDecimal("10"), new BigDecimal("150"));
        when(holdingRepository.findByIdAndUserId(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(new Ticker("AAPL")))
                .thenReturn(Optional.of(assetPrice("AAPL", new BigDecimal("200"))));

        useCase.execute(new CloseHoldingCommand(USER_ID, holding.id(), DESTINATION_CBU));

        ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);
        verify(financesGateway).recordSaleProceeds(eq(USER_ID), eq(DESTINATION_CBU), moneyCaptor.capture());
        assertThat(moneyCaptor.getValue().amount()).isEqualByComparingTo(new BigDecimal("2000"));

        verify(holdingRepository).delete(holding.id());

        ArgumentCaptor<HoldingClosedEvent> eventCaptor = ArgumentCaptor.forClass(HoldingClosedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        HoldingClosedEvent event = eventCaptor.getValue();
        assertThat(event.ticker().value()).isEqualTo("AAPL");
        assertThat(event.proceedsAmount().amount()).isEqualByComparingTo(new BigDecimal("2000"));
    }

    @Test
    void close_fallsBackToAvgPrice_whenNoPriceInRepository() {
        Holding holding = holding("AAPL", new BigDecimal("5"), new BigDecimal("100"));
        when(holdingRepository.findByIdAndUserId(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(any(Ticker.class))).thenReturn(Optional.empty());

        useCase.execute(new CloseHoldingCommand(USER_ID, holding.id(), DESTINATION_CBU));

        ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);
        verify(financesGateway).recordSaleProceeds(eq(USER_ID), eq(DESTINATION_CBU), moneyCaptor.capture());
        assertThat(moneyCaptor.getValue().amount()).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    void close_throwsResourceNotFoundException_whenHoldingNotFound() {
        when(holdingRepository.findByIdAndUserId(any(HoldingId.class), eq(USER_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.execute(new CloseHoldingCommand(USER_ID, new HoldingId(999L), DESTINATION_CBU)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void close_throwsFinancesServiceException_whenFinancesFails() {
        Holding holding = holding("AAPL", new BigDecimal("10"), new BigDecimal("150"));
        when(holdingRepository.findByIdAndUserId(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(any(Ticker.class))).thenReturn(Optional.empty());
        doThrow(new FinancesServiceException("Finances down", null))
                .when(financesGateway).recordSaleProceeds(any(), any(), any());

        assertThatThrownBy(() ->
                useCase.execute(new CloseHoldingCommand(USER_ID, holding.id(), DESTINATION_CBU)))
                .isInstanceOf(FinancesServiceException.class);

        verify(holdingRepository, never()).delete(any());
    }

    @Test
    void close_nullDestination_skipsFinances_andPublishesEvent() {
        Holding holding = holding("AAPL", new BigDecimal("10"), new BigDecimal("150"));
        when(holdingRepository.findByIdAndUserId(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(any(Ticker.class))).thenReturn(Optional.empty());

        useCase.execute(new CloseHoldingCommand(USER_ID, holding.id(), null));

        verify(financesGateway, never()).recordSaleProceeds(any(), any(), any());
        verify(holdingRepository).delete(holding.id());

        ArgumentCaptor<HoldingClosedEvent> eventCaptor = ArgumentCaptor.forClass(HoldingClosedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().destinationCbu()).isNull();
    }

    @Test
    void close_booksBondProceedsPerHundredNominal() {
        Holding ao29 = new Holding(new HoldingId(43L), USER_ID, new BankNumber("017"),
                new Ticker("AO29"), "Bono 2029", AssetType.BOND,
                new HoldingQuantity(new BigDecimal("687")), Money.of(new BigDecimal("1435.78"), "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
        when(holdingRepository.findByIdAndUserId(ao29.id(), USER_ID)).thenReturn(Optional.of(ao29));
        when(assetPriceRepository.findByTicker(new Ticker("AO29")))
                .thenReturn(Optional.of(assetPrice("AO29", new BigDecimal("131700"))));

        useCase.execute(new CloseHoldingCommand(USER_ID, ao29.id(), DESTINATION_CBU));

        ArgumentCaptor<Money> proceeds = ArgumentCaptor.forClass(Money.class);
        verify(financesGateway).recordSaleProceeds(eq(USER_ID), eq(DESTINATION_CBU), proceeds.capture());
        assertThat(proceeds.getValue().amount()).isEqualByComparingTo("904779.00");
    }

    @Test
    void close_bondWithoutAPriceBooksItsCost() {
        Holding ao29 = new Holding(new HoldingId(44L), USER_ID, new BankNumber("017"),
                new Ticker("AO29"), "Bono 2029", AssetType.BOND,
                new HoldingQuantity(new BigDecimal("687")), Money.of(new BigDecimal("1435.78"), "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
        when(holdingRepository.findByIdAndUserId(ao29.id(), USER_ID)).thenReturn(Optional.of(ao29));
        when(assetPriceRepository.findByTicker(any(Ticker.class))).thenReturn(Optional.empty());

        useCase.execute(new CloseHoldingCommand(USER_ID, ao29.id(), DESTINATION_CBU));

        ArgumentCaptor<Money> proceeds = ArgumentCaptor.forClass(Money.class);
        verify(financesGateway).recordSaleProceeds(eq(USER_ID), eq(DESTINATION_CBU), proceeds.capture());
        assertThat(proceeds.getValue().amount()).isEqualByComparingTo("986380.86");
    }

    private static Holding holding(String ticker, BigDecimal quantity, BigDecimal avgPrice) {
        return new Holding(new HoldingId(42L), USER_ID, new BankNumber("007"),
                new Ticker(ticker), "Test Holding", AssetType.STOCK,
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
