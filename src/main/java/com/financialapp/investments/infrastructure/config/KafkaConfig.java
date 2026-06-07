package com.financialapp.investments.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic investmentsThresholdBreachedTopic() {
        return TopicBuilder.name("investments.threshold.breached")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
