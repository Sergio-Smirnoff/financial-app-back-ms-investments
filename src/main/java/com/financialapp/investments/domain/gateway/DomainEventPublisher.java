package com.financialapp.investments.domain.gateway;

import com.financialapp.investments.domain.common.DomainEvent;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
