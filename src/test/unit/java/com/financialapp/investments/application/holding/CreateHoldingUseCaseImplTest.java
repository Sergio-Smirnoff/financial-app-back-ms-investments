package com.financialapp.investments.application.holding;

import com.financialapp.investments.application.holding.impl.CreateHoldingUseCaseImpl;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.HoldingCreatedEvent;
import com.financialapp.investments.domain.exception.BanksServiceException;
import com.financialapp.investments.domain.gateway.BanksGateway;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.usecase.holding.command.CreateHoldingCommand;
import com.financialapp.investments.infrastructure.exception.InfrastructureException;
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
    private BanksGateway banksGateway;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private CreateHoldingUseCaseImpl useCase;

    private static final UserId USER_ID = new UserId(1L);
    private static final BanksAccountId BANK_ACCOUNT = new BanksAccountId(10L);
    private static final BanksAccountId FUNDING_ACCOUNT = new BanksAccountId(20L);
    private static final BankId BANK_ID = new BankId(1L);

    @Test
    void create_savesHoldingAndPublishesEvent() {
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(inv -> withId(inv.getArgument(0), 1L));

        Holding result = useCase.execute(createCommand(FUNDING_ACCOUNT));

        assertThat(result.id().value()).isEqualTo(1L);
        assertThat(result.ticker().value()).isEqualTo("AAPL");

        ArgumentCaptor<HoldingCreatedEvent> captor = ArgumentCaptor.forClass(HoldingCreatedEvent.class);
        verify(eventPublisher).publish(captor.capture());
        HoldingCreatedEvent event = captor.getValue();
        assertThat(event.ticker().value()).isEqualTo("AAPL");
        assertThat(event.totalCost().amount()).isEqualByComparingTo(new BigDecimal("1500"));
    }

    @Test
    void create_adjustsBalanceWithNegativeAmount() {
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(inv -> withId(inv.getArgument(0), 1L));

        useCase.execute(createCommand(FUNDING_ACCOUNT));

        ArgumentCaptor<Money> moneyCaptor = ArgumentCaptor.forClass(Money.class);
        verify(banksGateway).adjustBalance(eq(FUNDING_ACCOUNT), moneyCaptor.capture());
        assertThat(moneyCaptor.getValue().amount()).isEqualByComparingTo(new BigDecimal("-1500"));
    }

    @Test
    void create_throwsBanksServiceException_whenBanksGatewayFails() {
        doThrow(new InfrastructureException("Banks down"))
                .when(banksGateway).adjustBalance(any(), any());

        assertThatThrownBy(() -> useCase.execute(createCommand(FUNDING_ACCOUNT)))
                .isInstanceOf(BanksServiceException.class);

        verify(holdingRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void create_withNullFundingAccount_doesNotCallBanksGateway() {
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(inv -> withId(inv.getArgument(0), 1L));

        useCase.execute(createCommand(null));

        verifyNoInteractions(banksGateway);
        verify(holdingRepository).save(any(Holding.class));
    }

    private static CreateHoldingCommand createCommand(BanksAccountId fundingAccount) {
        return new CreateHoldingCommand(
                USER_ID, BANK_ACCOUNT, BANK_ID,
                new Ticker("AAPL"), "Apple Inc", AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("10")),
                Money.of(new BigDecimal("150"), "ARS"),
                ThresholdConfig.disabled(), fundingAccount);
    }

    private static Holding withId(Holding h, long id) {
        return new Holding(new HoldingId(id), h.userId(), h.bankAccountId(), h.bankId(),
                h.ticker(), h.name(), h.assetType(), h.quantity(), h.avgPurchasePrice(),
                h.thresholdConfig(), h.notificationTimestamps(), h.createdAt(), h.updatedAt());
    }
}
