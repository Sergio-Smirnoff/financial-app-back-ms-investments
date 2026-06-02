package com.financialapp.investments.infrastructure.messaging;

import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.usecase.price.EvaluateThresholdsUseCase;
import com.financialapp.investments.infrastructure.persistence.entity.AssetPriceJpaEntity;
import com.financialapp.investments.infrastructure.persistence.entity.HoldingJpaEntity;
import com.financialapp.investments.support.AbstractKafkaIT;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end producer test on an embedded broker: a holding that breaches its gain threshold
 * causes a {@code investment.threshold.reached} event to be published after the transaction commits.
 */
class ThresholdEventPublishIT extends AbstractKafkaIT {

    private static final long USER = 700L;

    @Autowired
    EvaluateThresholdsUseCase evaluateThresholdsUseCase;

    @Test
    void gainThresholdBreached_publishesThresholdEvent() {
        LocalDateTime now = LocalDateTime.now();
        holdingRepository.save(HoldingJpaEntity.builder()
                .userId(USER).bankAccountId(1L).bankId(1L)
                .ticker("GGAL").name("Grupo Galicia").assetType(AssetType.STOCK)
                .quantity(new BigDecimal("10")).avgPurchasePrice(new BigDecimal("100"))
                .currency("ARS").notifyGainThresholdPct(new BigDecimal("5.00"))
                .createdAt(now).updatedAt(now)
                .build());
        assetPriceRepository.save(AssetPriceJpaEntity.builder()
                .ticker("GGAL").assetType(AssetType.STOCK)
                .lastPrice(new BigDecimal("130")).currency("ARS")
                .pricedAt(now).updatedAt(now)
                .build());

        try (Consumer<String, String> consumer = newConsumer("threshold-it", THRESHOLD_TOPIC)) {
            // @Transactional use case -> AFTER_COMMIT listener publishes to Kafka.
            evaluateThresholdsUseCase.execute();

            ConsumerRecord<String, String> record =
                    KafkaTestUtils.getSingleRecord(consumer, THRESHOLD_TOPIC, Duration.ofSeconds(15));

            assertThat(record.key()).isEqualTo(String.valueOf(USER));
            assertThat(record.value()).contains("GGAL").contains("GAIN");
        }
    }
}
