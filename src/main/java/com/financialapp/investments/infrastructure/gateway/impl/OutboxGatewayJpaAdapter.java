package com.financialapp.investments.infrastructure.gateway.impl;

import com.financialapp.commons.messaging.domain.gateway.OutboxGateway;
import com.financialapp.commons.messaging.domain.model.EventId;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import com.financialapp.investments.infrastructure.persistence.entity.OutboxEventEntity;
import com.financialapp.investments.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxGatewayJpaAdapter implements OutboxGateway {

    private final OutboxEventJpaRepository repository;

    @Override
    public void save(OutboxRecord record) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId(record.eventId().value());
        entity.setTopic(record.topic());
        entity.setAggregateKey(record.key());
        entity.setCeType(record.type().value());
        entity.setCeSource(record.source());
        entity.setDataSchema(record.dataSchema());
        entity.setDataJson(record.dataJson());
        entity.setSent(false);
        repository.save(entity);
    }

    @Override
    public List<OutboxRecord> findUnsent(int batchSize) {
        return repository.findBySentFalseOrderByIdAsc(Limit.of(batchSize)).stream()
                .map(e -> new OutboxRecord(
                        new EventId(e.getEventId()),
                        e.getTopic(),
                        e.getAggregateKey(),
                        new EventType(e.getCeType()),
                        e.getCeSource(),
                        e.getDataSchema(),
                        e.getDataJson()))
                .toList();
    }

    @Override
    public void markSent(EventId eventId) {
        repository.findByEventId(eventId.value()).ifPresent(entity -> {
            entity.setSent(true);
            entity.setSentAt(LocalDateTime.now());
            repository.save(entity);
        });
    }
}
