package com.financialapp.investments.infrastructure.messaging.mapper;

import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.Direction;
import com.financialapp.investments.domain.event.HoldingClosedEvent;
import com.financialapp.investments.domain.event.HoldingCreatedEvent;
import com.financialapp.investments.domain.event.HoldingUpdatedEvent;
import com.financialapp.investments.domain.event.PriceThresholdBreachedEvent;
import com.financialapp.investments.domain.model.holding.BanksAccountId;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.infrastructure.messaging.TransactionalKafkaEvent;
import com.financialapp.investments.infrastructure.messaging.payload.InvestmentThresholdPayload;
import com.financialapp.investments.infrastructure.messaging.payload.PaymentPayload;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvestmentKafkaMapperTest {

    private final InvestmentKafkaMapper mapper = new InvestmentKafkaMapper();

    private static final UserId USER = new UserId(7L);
    private static final HoldingId HID = new HoldingId(42L);
    private static final BanksAccountId ACC = new BanksAccountId(10L);
    private static final Ticker TIC = new Ticker("AAPL");
    private static final Money ARS_100 = Money.of(new BigDecimal("100"), "ARS");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void priceThresholdBreached_mapsToThresholdTopic() {
        PriceThresholdBreachedEvent e = new PriceThresholdBreachedEvent(HID, USER, TIC,
                "Apple Inc", Direction.GAIN, new BigDecimal("10"), new BigDecimal("12"),
                ARS_100, ARS_100, NOW);
        List<TransactionalKafkaEvent> out = mapper.toWireEvents(e);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).topic()).isEqualTo("investment.threshold.reached");
        assertThat(out.get(0).key()).isEqualTo("7");
        InvestmentThresholdPayload p = (InvestmentThresholdPayload) out.get(0).payload();
        assertThat(p.getUserId()).isEqualTo(7L);
        assertThat(p.getData().getTicker()).isEqualTo("AAPL");
        assertThat(p.getData().getDirection()).isEqualTo("GAIN");
        assertThat(p.getData().getCurrency()).isEqualTo("ARS");
    }

    @Test
    void holdingCreated_withFundingAccount_mapsToPaymentTopic_negatedAmount() {
        HoldingCreatedEvent e = new HoldingCreatedEvent(HID, USER, TIC, AssetType.STOCK,
                ACC, ACC, new HoldingQuantity(BigDecimal.ONE), ARS_100, ARS_100, NOW);
        List<TransactionalKafkaEvent> out = mapper.toWireEvents(e);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).topic()).isEqualTo("bank.payment.recorded");
        PaymentPayload p = (PaymentPayload) out.get(0).payload();
        assertThat(p.getAmount()).isEqualByComparingTo("-100");
        assertThat(p.getCurrency()).isEqualTo("ARS");
        assertThat(p.getDescription()).contains("Investment purchase").contains("AAPL");
    }

    @Test
    void holdingCreated_nullFundingAccount_emitsNothing() {
        HoldingCreatedEvent e = new HoldingCreatedEvent(HID, USER, TIC, AssetType.STOCK,
                ACC, null, new HoldingQuantity(BigDecimal.ONE), ARS_100, ARS_100, NOW);
        assertThat(mapper.toWireEvents(e)).isEmpty();
    }

    @Test
    void holdingUpdated_withFundingAccount_emitsNegatedCostDiff() {
        HoldingUpdatedEvent e = new HoldingUpdatedEvent(HID, USER, TIC, ACC, ACC,
                new HoldingQuantity(new BigDecimal("2")),
                new HoldingQuantity(BigDecimal.ONE),
                ARS_100, ARS_100, NOW);
        List<TransactionalKafkaEvent> out = mapper.toWireEvents(e);
        assertThat(out).hasSize(1);
        PaymentPayload p = (PaymentPayload) out.get(0).payload();
        assertThat(p.getAmount()).isEqualByComparingTo("-100");
        assertThat(p.getDescription()).contains("Investment update");
    }

    @Test
    void holdingUpdated_nullFundingAccount_emitsNothing() {
        HoldingUpdatedEvent e = new HoldingUpdatedEvent(HID, USER, TIC, ACC, null,
                new HoldingQuantity(BigDecimal.ONE), new HoldingQuantity(BigDecimal.ONE),
                ARS_100, ARS_100, NOW);
        assertThat(mapper.toWireEvents(e)).isEmpty();
    }

    @Test
    void holdingClosed_emitsPositiveProceeds() {
        HoldingClosedEvent e = new HoldingClosedEvent(HID, USER, TIC, ACC, ACC, ARS_100, NOW);
        List<TransactionalKafkaEvent> out = mapper.toWireEvents(e);
        assertThat(out).hasSize(1);
        PaymentPayload p = (PaymentPayload) out.get(0).payload();
        assertThat(p.getAmount()).isEqualByComparingTo("100");
        assertThat(p.getDescription()).contains("Investment sale");
    }

    @Test
    void holdingClosed_nullDepositAccountId_emitsNothing() {
        HoldingClosedEvent e = new HoldingClosedEvent(HID, USER, TIC, ACC, null, ARS_100, NOW);
        assertThat(mapper.toWireEvents(e)).isEmpty();
    }

    @Test
    void unknownEventType_emitsNothing() {
        DomainEvent unknown = new DomainEvent() {};
        assertThat(mapper.toWireEvents(unknown)).isEmpty();
    }
}
