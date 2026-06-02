package com.financialapp.investments.application.holding;

import com.financialapp.investments.application.holding.impl.UpdateHoldingUseCaseImpl;
import com.financialapp.investments.domain.common.model.Cbu;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.HoldingUpdatedEvent;
import com.financialapp.investments.domain.exception.FinancesServiceException;
import com.financialapp.investments.domain.exception.ResourceNotFoundException;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.gateway.FinancesGateway;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.usecase.holding.command.UpdateHoldingCommand;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateHoldingUseCaseImplTest {

    @Mock private HoldingRepository holdingRepository;
    @Mock private FinancesGateway financesGateway;
    @Mock private DomainEventPublisher eventPublisher;
    @InjectMocks private UpdateHoldingUseCaseImpl useCase;

    private static final UserId USER = new UserId(1L);
    private static final HoldingId HID = new HoldingId(42L);
    private static final Cbu ACC = new Cbu("0070009000000000000017");
    private static final Cbu FUNDING = new Cbu("0070009000000000000099");
    private static final Ticker TIC = new Ticker("AAPL");

    @Test
    void execute_updatesHolding_recordsCostDifference_publishesEvent() {
        Holding existing = holding(new BigDecimal("10"), new BigDecimal("100"));
        when(holdingRepository.findByIdAndUserId(HID, USER)).thenReturn(Optional.of(existing));
        when(holdingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateHoldingCommand cmd = new UpdateHoldingCommand(
                USER, HID, ACC, TIC, "Apple updated", AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("20")),
                Money.of(new BigDecimal("150"), "ARS"),
                ThresholdConfig.disabled(), FUNDING);

        Holding saved = useCase.execute(cmd);

        assertThat(saved.name()).isEqualTo("Apple updated");
        assertThat(saved.quantity().value()).isEqualByComparingTo("20");

        ArgumentCaptor<Money> moneyCap = ArgumentCaptor.forClass(Money.class);
        verify(financesGateway).recordPurchase(eq(USER), eq(FUNDING), moneyCap.capture());
        assertThat(moneyCap.getValue().amount()).isEqualByComparingTo("2000");

        ArgumentCaptor<HoldingUpdatedEvent> eventCap = ArgumentCaptor.forClass(HoldingUpdatedEvent.class);
        verify(eventPublisher).publish(eventCap.capture());
        HoldingUpdatedEvent event = eventCap.getValue();
        assertThat(event.previousQuantity().value()).isEqualByComparingTo("10");
        assertThat(event.newQuantity().value()).isEqualByComparingTo("20");
        assertThat(event.costDifference().amount()).isEqualByComparingTo("2000");
    }

    @Test
    void execute_holdingNotFound_throws() {
        when(holdingRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());
        UpdateHoldingCommand cmd = new UpdateHoldingCommand(USER, HID, ACC, TIC, "n",
                AssetType.STOCK, new HoldingQuantity(BigDecimal.ONE),
                Money.of(BigDecimal.ONE, "ARS"), ThresholdConfig.disabled(), FUNDING);
        assertThatThrownBy(() -> useCase.execute(cmd)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(financesGateway);
    }

    @Test
    void execute_financesFailure_propagates_andDoesNotSave() {
        Holding existing = holding(new BigDecimal("1"), new BigDecimal("1"));
        when(holdingRepository.findByIdAndUserId(HID, USER)).thenReturn(Optional.of(existing));
        doThrow(new FinancesServiceException("finances down", null))
                .when(financesGateway).recordPurchase(any(), any(), any());

        UpdateHoldingCommand cmd = new UpdateHoldingCommand(USER, HID, ACC, TIC, "n",
                AssetType.STOCK, new HoldingQuantity(new BigDecimal("5")),
                Money.of(new BigDecimal("10"), "ARS"), ThresholdConfig.disabled(), FUNDING);

        assertThatThrownBy(() -> useCase.execute(cmd)).isInstanceOf(FinancesServiceException.class);
        verify(holdingRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void execute_nullFundingAccount_skipsFinances_andSaves() {
        Holding existing = holding(new BigDecimal("10"), new BigDecimal("100"));
        when(holdingRepository.findByIdAndUserId(HID, USER)).thenReturn(Optional.of(existing));
        when(holdingRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateHoldingCommand cmd = new UpdateHoldingCommand(
                USER, HID, ACC, TIC, "Apple updated", AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("20")),
                Money.of(new BigDecimal("150"), "ARS"),
                ThresholdConfig.disabled(), null);

        Holding saved = useCase.execute(cmd);

        assertThat(saved.name()).isEqualTo("Apple updated");
        verifyNoInteractions(financesGateway);
        verify(holdingRepository).save(any());
        verify(eventPublisher).publish(any(HoldingUpdatedEvent.class));
    }

    private static Holding holding(BigDecimal qty, BigDecimal price) {
        return new Holding(HID, USER, ACC, TIC, "Apple", AssetType.STOCK,
                new HoldingQuantity(qty), Money.of(price, "ARS"),
                ThresholdConfig.disabled(), NotificationTimestamps.empty(),
                LocalDateTime.now(), LocalDateTime.now());
    }
}
