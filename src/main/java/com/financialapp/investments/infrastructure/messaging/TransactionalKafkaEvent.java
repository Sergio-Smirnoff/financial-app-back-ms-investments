package com.financialapp.investments.infrastructure.messaging;

public record TransactionalKafkaEvent(String topic, String key, Object payload) {}
