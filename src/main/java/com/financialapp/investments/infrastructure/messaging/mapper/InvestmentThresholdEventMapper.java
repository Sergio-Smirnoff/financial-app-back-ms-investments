package com.financialapp.investments.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.commons.messaging.domain.gateway.TypedDomainEventMapper;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import com.financialapp.investments.domain.event.PriceThresholdBreachedEvent;
import com.financialapp.investments.infrastructure.messaging.payload.InvestmentThresholdData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class InvestmentThresholdEventMapper extends TypedDomainEventMapper<PriceThresholdBreachedEvent> {

    private static final String TOPIC = "investments.threshold.breached";
    private static final String SCHEMA =
            "https://schemas.financial-app/investments/threshold-breached/v1";
    private static final String SOURCE = "ms-investments";

    private final ObjectMapper objectMapper;

    public InvestmentThresholdEventMapper(ObjectMapper objectMapper) {
        super(PriceThresholdBreachedEvent.class);
        this.objectMapper = objectMapper;
    }

    @Override
    protected List<OutboxRecord> mapTyped(PriceThresholdBreachedEvent event) {
        InvestmentThresholdData data = new InvestmentThresholdData(
                event.userId().value(),
                event.holdingId().value(),
                event.ticker().value(),
                event.holdingName(),
                event.direction().name(),
                event.thresholdPct(),
                event.actualPct(),
                event.currentPrice().amount(),
                event.avgPurchasePrice().amount(),
                event.currentPrice().currency().getCurrencyCode());

        return List.of(OutboxRecord.create(
                TOPIC,
                String.valueOf(event.userId().value()),
                new EventType(TOPIC),
                SOURCE,
                SCHEMA,
                serialize(data)));
    }

    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize InvestmentThresholdData", ex);
        }
    }
}
