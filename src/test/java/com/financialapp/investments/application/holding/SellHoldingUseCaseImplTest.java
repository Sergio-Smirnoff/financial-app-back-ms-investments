package com.financialapp.investments.application.holding;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.commons.core.domain.model.IvaTreatment;
import com.financialapp.investments.application.holding.impl.SellHoldingUseCaseImpl;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.HoldingClosedEvent;
import com.financialapp.investments.domain.event.HoldingUpdatedEvent;
import com.financialapp.investments.domain.exception.FinancesServiceException;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.exception.holding.HoldingSaleExceedsQuantityException;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.gateway.FinancesGateway;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.fee.BrokerFeeScheduleId;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.usecase.holding.command.SellHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.response.HoldingSaleResult;
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
class SellHoldingUseCaseImplTest {

    @Mock private HoldingRepository holdingRepository;
    @Mock private AssetPriceRepository assetPriceRepository;
    @Mock private FinancesGateway financesGateway;
    @Mock private DomainEventPublisher eventPublisher;
    @Mock private BrokerFeeScheduleRepository brokerFeeScheduleRepository;

    @InjectMocks private SellHoldingUseCaseImpl useCase;

    private static final UserId USER_ID = new UserId(1L);
    private static final Cbu DESTINATION_CBU = new Cbu("0070009000000000000099");

    @Test
    void sellAll_recordsSaleProceeds_andPublishesTheClosedEvent() {
        Holding holding = holding("AAPL", "10", "150");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(new Ticker("AAPL"))).thenReturn(Optional.of(assetPrice("AAPL", "200")));

        HoldingSaleResult sale = useCase.execute(sellAll(holding, DESTINATION_CBU));

        assertThat(bookedAmount()).isEqualByComparingTo("2000");
        verify(holdingRepository).delete(holding.id());
        verify(holdingRepository, never()).save(any());
        ArgumentCaptor<HoldingClosedEvent> event = ArgumentCaptor.forClass(HoldingClosedEvent.class);
        verify(eventPublisher).publish(event.capture());
        assertThat(event.getValue().ticker().value()).isEqualTo("AAPL");
        assertThat(event.getValue().proceedsAmount().amount()).isEqualByComparingTo("2000");
        assertThat(sale.closed()).isTrue();
        assertThat(sale.soldQuantity().value()).isEqualByComparingTo("10");
    }

    @Test
    void sellAll_fallsBackToTheAverageCost_whenNoPriceIsStored() {
        Holding holding = holding("AAPL", "5", "100");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(any(Ticker.class))).thenReturn(Optional.empty());

        useCase.execute(sellAll(holding, DESTINATION_CBU));

        assertThat(bookedAmount()).isEqualByComparingTo("500");
    }

    @Test
    void sell_readsTheHoldingUnderAWriteLock() {
        Holding holding = holding("AAPL", "10", "150");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(holdingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(new SellHoldingCommand(
                USER_ID, holding.id(), new HoldingQuantity(new BigDecimal("4")), new BigDecimal("200"), DESTINATION_CBU));

        verify(holdingRepository).findByIdAndUserIdForUpdate(holding.id(), USER_ID);
        verify(holdingRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    void sell_throwsResourceNotFound_whenTheHoldingIsNotTheCallers() {
        when(holdingRepository.findByIdAndUserIdForUpdate(any(HoldingId.class), eq(USER_ID))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                new SellHoldingCommand(USER_ID, new HoldingId(999L), null, null, DESTINATION_CBU)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void sell_keepsTheHolding_whenFinancesFails() {
        Holding holding = holding("AAPL", "10", "150");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(any(Ticker.class))).thenReturn(Optional.empty());
        doThrow(new FinancesServiceException("Finances down", null))
                .when(financesGateway).recordSaleProceeds(any(), any(), any());

        assertThatThrownBy(() -> useCase.execute(sellAll(holding, DESTINATION_CBU)))
                .isInstanceOf(FinancesServiceException.class);

        verify(holdingRepository, never()).delete(any());
        verify(holdingRepository, never()).save(any());
    }

    @Test
    void sellAll_withoutADestination_booksNothing_andStillCloses() {
        Holding holding = holding("AAPL", "10", "150");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(assetPriceRepository.findByTicker(any(Ticker.class))).thenReturn(Optional.empty());

        useCase.execute(sellAll(holding, null));

        verify(financesGateway, never()).recordSaleProceeds(any(), any(), any());
        verify(holdingRepository).delete(holding.id());
        ArgumentCaptor<HoldingClosedEvent> event = ArgumentCaptor.forClass(HoldingClosedEvent.class);
        verify(eventPublisher).publish(event.capture());
        assertThat(event.getValue().destinationCbu()).isNull();
    }

    @Test
    void sellAll_booksBondProceedsPerHundredNominal() {
        Holding ao29 = bond(43L);
        when(holdingRepository.findByIdAndUserIdForUpdate(ao29.id(), USER_ID)).thenReturn(Optional.of(ao29));
        when(assetPriceRepository.findByTicker(new Ticker("AO29"))).thenReturn(Optional.of(assetPrice("AO29", "131700")));

        useCase.execute(sellAll(ao29, DESTINATION_CBU));

        assertThat(bookedAmount()).isEqualByComparingTo("904779.00");
    }

    @Test
    void sellAll_bondWithoutAPriceBooksItsCost() {
        Holding ao29 = bond(44L);
        when(holdingRepository.findByIdAndUserIdForUpdate(ao29.id(), USER_ID)).thenReturn(Optional.of(ao29));
        when(assetPriceRepository.findByTicker(any(Ticker.class))).thenReturn(Optional.empty());

        useCase.execute(sellAll(ao29, DESTINATION_CBU));

        assertThat(bookedAmount()).isEqualByComparingTo("986380.86");
    }

    @Test
    void sellPart_atAManualPrice_booksQuantityTimesPrice_andKeepsTheAverageCost() {
        Holding holding = holding("AAPL", "10", "150");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(holdingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        HoldingSaleResult sale = useCase.execute(new SellHoldingCommand(
                USER_ID, holding.id(), new HoldingQuantity(new BigDecimal("4")), new BigDecimal("200"), DESTINATION_CBU));

        assertThat(bookedAmount()).isEqualByComparingTo("800");
        verify(assetPriceRepository, never()).findByTicker(any());
        ArgumentCaptor<Holding> saved = ArgumentCaptor.forClass(Holding.class);
        verify(holdingRepository).save(saved.capture());
        assertThat(saved.getValue().quantity().value()).isEqualByComparingTo("6");
        assertThat(saved.getValue().avgPurchasePrice().amount()).isEqualByComparingTo("150");
        verify(holdingRepository, never()).delete(any());
        ArgumentCaptor<HoldingUpdatedEvent> event = ArgumentCaptor.forClass(HoldingUpdatedEvent.class);
        verify(eventPublisher).publish(event.capture());
        assertThat(event.getValue().previousQuantity().value()).isEqualByComparingTo("10");
        assertThat(event.getValue().newQuantity().value()).isEqualByComparingTo("6");
        assertThat(event.getValue().costDifference().amount()).isEqualByComparingTo("-600");
        assertThat(sale.closed()).isFalse();
        assertThat(sale.remainingQuantity().value()).isEqualByComparingTo("6");
        assertThat(sale.proceeds().amount()).isEqualByComparingTo("800");
    }

    @Test
    void sellPart_atTheMarketPrice_usesTheStoredQuote() {
        Holding holding = holding("AAPL", "10", "150");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(holdingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(assetPriceRepository.findByTicker(new Ticker("AAPL"))).thenReturn(Optional.of(assetPrice("AAPL", "210")));

        useCase.execute(new SellHoldingCommand(
                USER_ID, holding.id(), new HoldingQuantity(new BigDecimal("4")), null, DESTINATION_CBU));

        assertThat(bookedAmount()).isEqualByComparingTo("840");
    }

    @Test
    void sellPart_ofABond_readsTheManualPricePerHundredNominal() {
        Holding ao29 = bond(45L);
        when(holdingRepository.findByIdAndUserIdForUpdate(ao29.id(), USER_ID)).thenReturn(Optional.of(ao29));
        when(holdingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        useCase.execute(new SellHoldingCommand(
                USER_ID, ao29.id(), new HoldingQuantity(new BigDecimal("100")), new BigDecimal("131700"), DESTINATION_CBU));

        assertThat(bookedAmount()).isEqualByComparingTo("131700");
    }

    @Test
    void sellPart_withoutAFeeSchedule_booksAFractionalSaleRoundedToCents() {
        Holding holding = holding("AAPL", "10", "150");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(holdingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(brokerFeeScheduleRepository.findFor(any(), any())).thenReturn(Optional.empty());

        HoldingSaleResult sale = useCase.execute(new SellHoldingCommand(
                USER_ID, holding.id(), new HoldingQuantity(new BigDecimal("7")), new BigDecimal("10.123"), DESTINATION_CBU));

        assertThat(bookedAmount()).isEqualTo(new BigDecimal("70.86"));
        assertThat(sale.bookedAmount().amount()).isEqualTo(new BigDecimal("70.86"));
        assertThat(sale.proceeds().amount()).isEqualByComparingTo("70.861");
    }

    @Test
    void sellPart_withASellFee_booksTheProceedsNetOfTheFee() {
        Holding holding = holding("AAPL", "10", "150");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(holdingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(brokerFeeScheduleRepository.findFor(new BankNumber("007"), AssetType.STOCK))
                .thenReturn(Optional.of(feeSchedule("0.50", null)));

        HoldingSaleResult sale = useCase.execute(new SellHoldingCommand(
                USER_ID, holding.id(), new HoldingQuantity(new BigDecimal("4")), new BigDecimal("200"), DESTINATION_CBU));

        assertThat(bookedAmount()).isEqualTo(new BigDecimal("796.00"));
        assertThat(sale.bookedAmount().amount()).isEqualTo(new BigDecimal("796.00"));
        assertThat(sale.proceeds().amount()).isEqualByComparingTo("800");
    }

    @Test
    void sellPart_whenTheFeeExceedsTheProceeds_booksZero() {
        Holding holding = holding("AAPL", "10", "150");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));
        when(holdingRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(brokerFeeScheduleRepository.findFor(new BankNumber("007"), AssetType.STOCK))
                .thenReturn(Optional.of(feeSchedule("0.50", "1000.00")));

        HoldingSaleResult sale = useCase.execute(new SellHoldingCommand(
                USER_ID, holding.id(), new HoldingQuantity(new BigDecimal("4")), new BigDecimal("200"), DESTINATION_CBU));

        assertThat(bookedAmount()).isEqualByComparingTo("0");
        assertThat(sale.bookedAmount().amount()).isEqualByComparingTo("0");
        assertThat(sale.proceeds().amount()).isEqualByComparingTo("800");
    }

    @Test
    void sellingMoreThanHeld_movesNoMoney_andKeepsTheHolding() {
        Holding holding = holding("AAPL", "10", "150");
        when(holdingRepository.findByIdAndUserIdForUpdate(holding.id(), USER_ID)).thenReturn(Optional.of(holding));

        assertThatThrownBy(() -> useCase.execute(new SellHoldingCommand(
                USER_ID, holding.id(), new HoldingQuantity(new BigDecimal("11")), new BigDecimal("200"), DESTINATION_CBU)))
                .isInstanceOf(HoldingSaleExceedsQuantityException.class);

        verify(financesGateway, never()).recordSaleProceeds(any(), any(), any());
        verify(holdingRepository, never()).save(any());
        verify(holdingRepository, never()).delete(any());
        verify(eventPublisher, never()).publish(any());
    }

    private BigDecimal bookedAmount() {
        ArgumentCaptor<Money> money = ArgumentCaptor.forClass(Money.class);
        verify(financesGateway).recordSaleProceeds(eq(USER_ID), eq(DESTINATION_CBU), money.capture());
        return money.getValue().amount();
    }

    private static SellHoldingCommand sellAll(Holding holding, Cbu destination) {
        return new SellHoldingCommand(USER_ID, holding.id(), null, null, destination);
    }

    private static Holding holding(String ticker, String quantity, String avgPrice) {
        return new Holding(new HoldingId(42L), USER_ID, new BankNumber("007"),
                new Ticker(ticker), "Test Holding", AssetType.STOCK,
                new HoldingQuantity(new BigDecimal(quantity)), Money.of(new BigDecimal(avgPrice), "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static Holding bond(long id) {
        return new Holding(new HoldingId(id), USER_ID, new BankNumber("017"),
                new Ticker("AO29"), "Bono 2029", AssetType.BOND,
                new HoldingQuantity(new BigDecimal("687")), Money.of(new BigDecimal("1435.78"), "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }

    private static BrokerFeeSchedule feeSchedule(String sellFeePct, String minimumFee) {
        return new BrokerFeeSchedule(new BrokerFeeScheduleId(1L), new BankNumber("007"), AssetType.STOCK,
                BigDecimal.ZERO, new BigDecimal(sellFeePct),
                minimumFee != null ? Money.of(new BigDecimal(minimumFee), "ARS") : null,
                BigDecimal.ZERO, IvaTreatment.EXEMPT);
    }

    private static AssetPrice assetPrice(String ticker, String price) {
        return new AssetPrice(new AssetPriceId(1L), new Ticker(ticker), AssetType.STOCK,
                new BigDecimal(price), "ARS", null, null, null, null, null,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
