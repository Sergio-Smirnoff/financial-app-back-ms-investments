package com.financialapp.investments.kafka.producer;

public record TransactionalKafkaEvent(String topic, String key, Object payload) {}
