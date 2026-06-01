package com.financialapp.investments.infrastructure.messaging;

import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.infrastructure.messaging.mapper.InvestmentKafkaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher springEventPublisher;
    private final InvestmentKafkaMapper mapper;

    @Override
    public void publish(DomainEvent event) {
        mapper.toWireEvents(event).forEach(springEventPublisher::publishEvent);
    }
}
