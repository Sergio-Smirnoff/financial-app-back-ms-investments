package com.financialapp.investments.infrastructure.messaging.mapper;

import com.financialapp.investments.domain.common.DomainEvent;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.Direction;
import com.financialapp.investments.domain.event.PriceThresholdBreachedEvent;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.infrastructure.messaging.TransactionalKafkaEvent;
import com.financialapp.investments.infrastructure.messaging.payload.InvestmentThresholdPayload;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvestmentKafkaMapperTest {

    private final InvestmentKafkaMapper mapper = new InvestmentKafkaMapper();

    private static final UserId USER = new UserId(7L);
    private static final HoldingId HID = new HoldingId(42L);
    private static final Ticker TIC = new Ticker("AAPL");
    private static final Money ARS_100 = Money.of(new BigDecimal("100"), "ARS");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void priceThresholdBreached_mapsToThresholdTopic() {
        PriceThresholdBreachedEvent e = new PriceThresholdBreachedEvent(HID, USER, TIC,
                "Apple Inc", Direction.GAIN, new BigDecimal("10"), new BigDecimal("12"),
                ARS_100, ARS_100, NOW);
        List<TransactionalKafkaEvent> out = mapper.toWireEvents(e);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).topic()).isEqualTo("investment.threshold.reached");
        assertThat(out.get(0).key()).isEqualTo("7");
        InvestmentThresholdPayload p = (InvestmentThresholdPayload) out.get(0).payload();
        assertThat(p.getUserId()).isEqualTo(7L);
        assertThat(p.getData().getTicker()).isEqualTo("AAPL");
        assertThat(p.getData().getDirection()).isEqualTo("GAIN");
        assertThat(p.getData().getCurrency()).isEqualTo("ARS");
    }

    @Test
    void unknownEventType_emitsNothing() {
        DomainEvent unknown = new DomainEvent() {};
        assertThat(mapper.toWireEvents(unknown)).isEmpty();
    }
}
