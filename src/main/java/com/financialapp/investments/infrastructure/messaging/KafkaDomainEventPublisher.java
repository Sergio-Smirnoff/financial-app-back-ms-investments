package com.financialapp.investments.infrastructure.messaging;

import com.financialapp.commons.messaging.infrastructure.messaging.relay.OutboxEventPublisher;
import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private final OutboxEventPublisher outboxEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        outboxEventPublisher.publish(event);
    }
}
