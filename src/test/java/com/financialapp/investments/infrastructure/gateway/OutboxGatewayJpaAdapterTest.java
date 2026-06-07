package com.financialapp.investments.infrastructure.gateway;

import com.financialapp.commons.messaging.domain.model.EventId;
import com.financialapp.commons.messaging.domain.model.EventType;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import com.financialapp.investments.infrastructure.persistence.entity.OutboxEventEntity;
import com.financialapp.investments.infrastructure.persistence.jpa.OutboxEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financialapp.investments.infrastructure.gateway.impl.OutboxGatewayJpaAdapter;

class OutboxGatewayJpaAdapterTest {

    private final OutboxEventJpaRepository repository = mock(OutboxEventJpaRepository.class);
    private final OutboxGatewayJpaAdapter adapter = new OutboxGatewayJpaAdapter(repository);

    @Test
    void save_mapsRecordToEntity() {
        OutboxRecord record = OutboxRecord.create(
                "investments.threshold.breached",
                "7",
                new EventType("investments.threshold.breached"),
                "ms-investments",
                "https://schemas.financial-app/investments/threshold-breached/v1",
                "{\"userId\":7}");

        adapter.save(record);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(repository).save(captor.capture());
        OutboxEventEntity saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo(record.eventId().value());
        assertThat(saved.getTopic()).isEqualTo("investments.threshold.breached");
        assertThat(saved.getAggregateKey()).isEqualTo("7");
        assertThat(saved.getCeType()).isEqualTo("investments.threshold.breached");
        assertThat(saved.getCeSource()).isEqualTo("ms-investments");
        assertThat(saved.isSent()).isFalse();
    }

    @Test
    void markSent_setsEntitySentFlag() {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId("some-id");
        entity.setSent(false);
        when(repository.findByEventId("some-id")).thenReturn(Optional.of(entity));

        adapter.markSent(new EventId("some-id"));

        assertThat(entity.isSent()).isTrue();
        assertThat(entity.getSentAt()).isNotNull();
        verify(repository).save(entity);
    }
}
