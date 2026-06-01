package com.financialapp.investments.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionalKafkaListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TransactionalKafkaEvent event) {
        log.info("Sending transactional Kafka event to topic={} for key={}", event.topic(), event.key());
        kafkaTemplate.send(event.topic(), event.key(), event.payload());
    }
}
