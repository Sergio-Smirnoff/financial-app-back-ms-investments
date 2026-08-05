package com.financialapp.investments.application.holding;

import com.financialapp.investments.application.holding.impl.CreateHoldingUseCaseImpl;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.HoldingCreatedEvent;
import com.financialapp.investments.domain.exception.FinancesServiceException;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.gateway.FinancesGateway;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.usecase.holding.command.CreateHoldingCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

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
class CreateHoldingUseCaseImplTest {

    @Mock
    private HoldingRepository holdingRepository;
    @Mock
    private FinancesGateway financesGateway;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private com.financialapp.investments.domain.repository.BrokerFeeScheduleRepository brokerFeeScheduleRepository;

    @InjectMocks
    private CreateHoldingUseCaseImpl useCase;

    private static final UserId USER_ID = new UserId(1L);
    private static final BankNumber BANK_NUMBER = new BankNumber("007");
    private static final Cbu FUNDING_CBU = new Cbu("0070009000000000000099");

    @Test
    void create_savesHoldingAndPublishesEvent() {
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(inv -> withId(inv.getArgument(0), 1L));

        Holding result = useCase.execute(createCommand(FUNDING_CBU));

        assertThat(result.id().value()).isEqualTo(1L);
        assertThat(result.ticker().value()).isEqualTo("AAPL");

        ArgumentCaptor<HoldingCreatedEvent> captor = ArgumentCaptor.forClass(HoldingCreatedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        HoldingCreatedEvent event = captor.getValue();
        assertThat(event.ticker().value()).isEqualTo("AAPL");
        assertThat(event.totalCost().amount()).isEqualByComparingTo(new BigDecimal("1500"));
    }

    @Test
    void create_recordsPurchaseForTotalCost() {
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(inv -> withId(inv.getArgument(0), 1L));

        useCase.execute(createCommand(FUNDING_CBU));

        ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);
        verify(financesGateway).recordPurchase(eq(USER_ID), eq(FUNDING_CBU), moneyCaptor.capture());
        assertThat(moneyCaptor.getValue().amount()).isEqualByComparingTo(new BigDecimal("1500"));
    }

    @Test
    void create_throwsFinancesServiceException_whenFinancesFails() {
        doThrow(new FinancesServiceException("Finances down", null))
                .when(financesGateway).recordPurchase(any(), any(), any());

        assertThatThrownBy(() -> useCase.execute(createCommand(FUNDING_CBU)))
                .isInstanceOf(FinancesServiceException.class);

        verify(holdingRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void create_withNullFundingAccount_doesNotCallFinances() {
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(inv -> withId(inv.getArgument(0), 1L));

        useCase.execute(createCommand(null));

        verifyNoInteractions(financesGateway);
        verify(holdingRepository).save(any(Holding.class));
    }

    private static CreateHoldingCommand createCommand(Cbu fundingCbu) {
        return new CreateHoldingCommand(
                USER_ID, BANK_NUMBER,
                new Ticker("AAPL"), "Apple Inc", AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("10")),
                Money.of(new BigDecimal("150"), "ARS"),
                ThresholdConfig.disabled(), fundingCbu);
    }

    private static Holding withId(Holding h, long id) {
        return new Holding(new HoldingId(id), h.userId(), h.bankNumber(),
                h.ticker(), h.name(), h.assetType(), h.quantity(), h.avgPurchasePrice(),
                h.thresholdConfig(), h.notificationTimestamps(), h.createdAt(), h.updatedAt());
    }
}
