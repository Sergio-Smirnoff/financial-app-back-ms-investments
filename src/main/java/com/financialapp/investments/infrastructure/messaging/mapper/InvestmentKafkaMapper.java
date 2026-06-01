package com.financialapp.investments.infrastructure.messaging.mapper;

import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.domain.event.*;
import com.financialapp.investments.infrastructure.messaging.payload.InvestmentThresholdPayload;
import com.financialapp.investments.infrastructure.messaging.payload.PaymentPayload;
import com.financialapp.investments.infrastructure.messaging.TransactionalKafkaEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class InvestmentKafkaMapper {

    private static final String TOPIC_THRESHOLD = "investment.threshold.reached";
    private static final String TOPIC_PAYMENT = "bank.payment.recorded";

    private final Map<Class<? extends DomainEvent>, Function<DomainEvent, List<TransactionalKafkaEvent>>> handlers = Map.of(
            PriceThresholdBreachedEvent.class, e -> List.of(toThresholdEvent((PriceThresholdBreachedEvent) e)),
            HoldingCreatedEvent.class,         e -> wrap(toPaymentEvent((HoldingCreatedEvent) e)),
            HoldingUpdatedEvent.class,         e -> wrap(toPaymentEvent((HoldingUpdatedEvent) e)),
            HoldingClosedEvent.class,          e -> wrap(toPaymentEvent((HoldingClosedEvent) e))
    );

    public List<TransactionalKafkaEvent> toWireEvents(DomainEvent event) {
        return handlers.getOrDefault(event.getClass(), e -> List.of()).apply(event);
    }

    private static List<TransactionalKafkaEvent> wrap(TransactionalKafkaEvent wire) {
        return wire != null ? List.of(wire) : List.of();
    }

    private TransactionalKafkaEvent toThresholdEvent(PriceThresholdBreachedEvent e) {
        InvestmentThresholdPayload payload = InvestmentThresholdPayload.builder()
                .userId(e.userId().value())
                .data(InvestmentThresholdPayload.Data.builder()
                        .holdingId(e.holdingId().value())
                        .ticker(e.ticker().value())
                        .name(e.holdingName())
                        .direction(e.direction().name())
                        .thresholdPct(e.thresholdPct())
                        .actualPct(e.actualPct())
                        .currentPrice(e.currentPrice().amount())
                        .avgPurchasePrice(e.avgPurchasePrice().amount())
                        .currency(e.currentPrice().currency().getCurrencyCode())
                        .build())
                .build();
        return new TransactionalKafkaEvent(
                TOPIC_THRESHOLD, String.valueOf(e.userId().value()), payload);
    }

    private TransactionalKafkaEvent toPaymentEvent(HoldingCreatedEvent e) {
        if (e.fundingAccountId() == null) return null;
        PaymentPayload payload = PaymentPayload.builder()
                .userId(e.userId().value())
                .accountId(e.fundingAccountId().value())
                .amount(e.totalCost().negate().amount())
                .currency(e.totalCost().currency().getCurrencyCode())
                .description("Investment purchase: " + e.ticker().value())
                .date(LocalDate.now())
                .build();
        return new TransactionalKafkaEvent(
                TOPIC_PAYMENT, String.valueOf(e.userId().value()), payload);
    }

    private TransactionalKafkaEvent toPaymentEvent(HoldingUpdatedEvent e) {
        if (e.fundingAccountId() == null) return null;
        PaymentPayload payload = PaymentPayload.builder()
                .userId(e.userId().value())
                .accountId(e.fundingAccountId().value())
                .amount(e.costDifference().negate().amount())
                .currency(e.costDifference().currency().getCurrencyCode())
                .description("Investment update: " + e.ticker().value())
                .date(LocalDate.now())
                .build();
        return new TransactionalKafkaEvent(
                TOPIC_PAYMENT, String.valueOf(e.userId().value()), payload);
    }

    private TransactionalKafkaEvent toPaymentEvent(HoldingClosedEvent e) {
        PaymentPayload payload = PaymentPayload.builder()
                .userId(e.userId().value())
                .accountId(e.depositAccountId().value())
                .amount(e.proceedsAmount().amount())
                .currency(e.proceedsAmount().currency().getCurrencyCode())
                .description("Investment sale proceeds: " + e.ticker().value())
                .date(LocalDate.now())
                .build();
        return new TransactionalKafkaEvent(
                TOPIC_PAYMENT, String.valueOf(e.userId().value()), payload);
    }
}
