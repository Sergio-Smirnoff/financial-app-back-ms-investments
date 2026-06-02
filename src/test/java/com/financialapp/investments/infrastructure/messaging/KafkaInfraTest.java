package com.financialapp.investments.infrastructure.messaging;

import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.infrastructure.messaging.mapper.InvestmentKafkaMapper;
import com.financialapp.investments.infrastructure.messaging.payload.InvestmentThresholdPayload;
import com.financialapp.investments.infrastructure.messaging.payload.PaymentPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaInfraTest {

    @Mock private ApplicationEventPublisher springEventPublisher;
    @Mock private InvestmentKafkaMapper mapper;
    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void domainEventPublisher_publishesEachWireEventToSpring() {
        KafkaDomainEventPublisher publisher = new KafkaDomainEventPublisher(springEventPublisher, mapper);
        TransactionalKafkaEvent w1 = new TransactionalKafkaEvent("topic-a", "k1", "p1");
        TransactionalKafkaEvent w2 = new TransactionalKafkaEvent("topic-b", "k2", "p2");
        DomainEvent event = new DomainEvent() {};
        when(mapper.toWireEvents(event)).thenReturn(List.of(w1, w2));

        publisher.publish(event);

        verify(springEventPublisher).publishEvent(w1);
        verify(springEventPublisher).publishEvent(w2);
    }

    @Test
    void domainEventPublisher_emptyWireList_doesNotPublish() {
        KafkaDomainEventPublisher publisher = new KafkaDomainEventPublisher(springEventPublisher, mapper);
        DomainEvent event = new DomainEvent() {};
        when(mapper.toWireEvents(event)).thenReturn(List.of());

        publisher.publish(event);

        verifyNoInteractions(springEventPublisher);
    }

    @Test
    void transactionalKafkaListener_sendsToKafka() {
        TransactionalKafkaListener listener = new TransactionalKafkaListener(kafkaTemplate);
        TransactionalKafkaEvent event = new TransactionalKafkaEvent("topic", "key", "payload");
        listener.handle(event);
        verify(kafkaTemplate).send("topic", "key", "payload");
    }

    @Test
    void transactionalKafkaEvent_recordAccessors() {
        TransactionalKafkaEvent e = new TransactionalKafkaEvent("t", "k", "p");
        assertThat(e.topic()).isEqualTo("t");
        assertThat(e.key()).isEqualTo("k");
        assertThat(e.payload()).isEqualTo("p");
    }

    @Test
    void paymentPayload_lombokBuilderAndAccessors() {
        PaymentPayload p = PaymentPayload.builder()
                .userId(1L).accountId(2L).amount(BigDecimal.TEN)
                .currency("ARS").description("d").date(null).build();
        assertThat(p.getUserId()).isEqualTo(1L);
        assertThat(p.getAccountId()).isEqualTo(2L);
        assertThat(p.getAmount()).isEqualByComparingTo("10");
        assertThat(p.getCurrency()).isEqualTo("ARS");

        PaymentPayload empty = new PaymentPayload();
        empty.setUserId(99L);
        assertThat(empty.getUserId()).isEqualTo(99L);
    }

    @Test
    void investmentThresholdPayload_builderDefaultsAndDataAccessors() {
        InvestmentThresholdPayload p = InvestmentThresholdPayload.builder()
                .userId(1L)
                .timestamp(Instant.EPOCH)
                .data(InvestmentThresholdPayload.Data.builder()
                        .holdingId(2L).ticker("X").name("n").direction("GAIN")
                        .thresholdPct(BigDecimal.ONE).actualPct(BigDecimal.ONE)
                        .currentPrice(BigDecimal.ONE).avgPurchasePrice(BigDecimal.ONE)
                        .currency("ARS").build())
                .build();
        assertThat(p.getEventType()).isEqualTo("INVESTMENT_THRESHOLD");
        assertThat(p.getUserId()).isEqualTo(1L);
        assertThat(p.getTimestamp()).isEqualTo(Instant.EPOCH);
        assertThat(p.getData().getHoldingId()).isEqualTo(2L);
        assertThat(p.getData().getCurrency()).isEqualTo("ARS");

        InvestmentThresholdPayload noArgs = new InvestmentThresholdPayload();
        assertThat(noArgs.getEventType()).isEqualTo("INVESTMENT_THRESHOLD");
        InvestmentThresholdPayload.Data noArgsData = new InvestmentThresholdPayload.Data();
        assertThat(noArgsData).isNotNull();
    }
}
