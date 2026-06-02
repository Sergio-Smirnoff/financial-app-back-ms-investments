package com.financialapp.investments.support;

import com.financialapp.investments.infrastructure.persistence.jpa.AssetPriceJpaRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.HoldingJpaRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

/**
 * Base for Kafka producer integration tests, backed by an embedded broker.
 *
 * <p>The {@code test} profile excludes Kafka auto-config; here it is re-enabled
 * (empty {@code spring.autoconfigure.exclude}) and the bootstrap servers are pointed
 * at the embedded broker, so a real {@link org.springframework.kafka.core.KafkaTemplate}
 * is wired and the {@code @TransactionalEventListener} actually publishes.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.autoconfigure.exclude=",
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.consumer.auto-offset-reset=earliest"
        })
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {AbstractKafkaIT.THRESHOLD_TOPIC})
public abstract class AbstractKafkaIT {

    public static final String THRESHOLD_TOPIC = "investment.threshold.reached";

    @Autowired protected EmbeddedKafkaBroker embeddedKafka;
    @Autowired protected HoldingJpaRepository holdingRepository;
    @Autowired protected AssetPriceJpaRepository assetPriceRepository;

    @BeforeEach
    void cleanDatabase() {
        assetPriceRepository.deleteAll();
        holdingRepository.deleteAll();
    }

    /** A String/String consumer subscribed from the beginning of the given topic. */
    protected Consumer<String, String> newConsumer(String group, String topic) {
        Map<String, Object> props = KafkaTestUtils.consumerProps(group, "true", embeddedKafka);
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, topic);
        return consumer;
    }
}
