package com.financialapp.investments.infrastructure.messaging.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.Direction;
import com.financialapp.investments.domain.event.PriceThresholdBreachedEvent;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.Ticker;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvestmentThresholdEventMapperTest {

    private static final UserId USER = new UserId(7L);
    private static final HoldingId HID = new HoldingId(42L);
    private static final Ticker TIC = new Ticker("AAPL");
    private static final Money ARS_100 = Money.of(new BigDecimal("100"), "ARS");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    private final InvestmentThresholdEventMapper mapper =
            new InvestmentThresholdEventMapper(new ObjectMapper());

    @Test
    void supportsOnlyPriceThresholdBreachedEvent() {
        PriceThresholdBreachedEvent event = new PriceThresholdBreachedEvent(
                HID, USER, TIC, "Apple Inc", Direction.GAIN,
                new BigDecimal("10"), new BigDecimal("12"),
                ARS_100, ARS_100, NOW);

        assertThat(mapper.supports(event)).isTrue();
        assertThat(mapper.supports(new Object())).isFalse();
    }

    @Test
    void mapsToCorrectOutboxRecord_topicAndType() {
        PriceThresholdBreachedEvent event = new PriceThresholdBreachedEvent(
                HID, USER, TIC, "Apple Inc", Direction.GAIN,
                new BigDecimal("10"), new BigDecimal("12"),
                ARS_100, ARS_100, NOW);

        List<OutboxRecord> records = mapper.toOutboxRecords(event);

        assertThat(records).hasSize(1);
        OutboxRecord r = records.get(0);
        assertThat(r.topic()).isEqualTo("investments.threshold.breached");
        assertThat(r.type().value()).isEqualTo("investments.threshold.breached");
        assertThat(r.source()).isEqualTo("ms-investments");
        assertThat(r.dataSchema())
                .isEqualTo("https://schemas.financial-app/investments/threshold-breached/v1");
    }

    @Test
    void mapsKeyToUserId() {
        PriceThresholdBreachedEvent event = new PriceThresholdBreachedEvent(
                HID, USER, TIC, "Apple Inc", Direction.GAIN,
                new BigDecimal("10"), new BigDecimal("12"),
                ARS_100, ARS_100, NOW);

        OutboxRecord r = mapper.toOutboxRecords(event).get(0);

        assertThat(r.key()).isEqualTo("7");
    }

    @Test
    void dataJsonContainsFlatFields_withUserIdAndNoNestedPayload() throws Exception {
        PriceThresholdBreachedEvent event = new PriceThresholdBreachedEvent(
                HID, USER, TIC, "Apple Inc", Direction.GAIN,
                new BigDecimal("10"), new BigDecimal("12"),
                ARS_100, ARS_100, NOW);

        OutboxRecord r = mapper.toOutboxRecords(event).get(0);
        ObjectMapper om = new ObjectMapper();
        var node = om.readTree(r.dataJson());

        assertThat(node.has("userId")).isTrue();
        assertThat(node.get("userId").asLong()).isEqualTo(7L);
        assertThat(node.has("holdingId")).isTrue();
        assertThat(node.get("holdingId").asLong()).isEqualTo(42L);
        assertThat(node.get("ticker").asText()).isEqualTo("AAPL");
        assertThat(node.get("name").asText()).isEqualTo("Apple Inc");
        assertThat(node.get("direction").asText()).isEqualTo("GAIN");
        assertThat(node.get("thresholdPct").decimalValue()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(node.get("actualPct").decimalValue()).isEqualByComparingTo(new BigDecimal("12"));
        assertThat(node.get("currentPrice").decimalValue()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(node.get("avgPurchasePrice").decimalValue()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(node.get("currency").asText()).isEqualTo("ARS");
        assertThat(node.has("data")).isFalse();
        assertThat(node.has("payload")).isFalse();
    }

    @Test
    void generatesNonBlankEventId() {
        PriceThresholdBreachedEvent event = new PriceThresholdBreachedEvent(
                HID, USER, TIC, "Apple Inc", Direction.LOSS,
                new BigDecimal("5"), new BigDecimal("7"),
                ARS_100, ARS_100, NOW);

        OutboxRecord r = mapper.toOutboxRecords(event).get(0);

        assertThat(r.eventId().value()).isNotBlank();
    }
}
