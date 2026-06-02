package com.financialapp.investments.infrastructure.messaging.mapper;

import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.domain.event.*;
import com.financialapp.investments.infrastructure.messaging.payload.InvestmentThresholdPayload;
import com.financialapp.investments.infrastructure.messaging.TransactionalKafkaEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class InvestmentKafkaMapper {

    private static final String TOPIC_THRESHOLD = "investment.threshold.reached";

    private final Map<Class<? extends DomainEvent>, Function<DomainEvent, List<TransactionalKafkaEvent>>> handlers = Map.of(
            PriceThresholdBreachedEvent.class, e -> List.of(toThresholdEvent((PriceThresholdBreachedEvent) e))
    );

    public List<TransactionalKafkaEvent> toWireEvents(DomainEvent event) {
        return handlers.getOrDefault(event.getClass(), e -> List.of()).apply(event);
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
}
