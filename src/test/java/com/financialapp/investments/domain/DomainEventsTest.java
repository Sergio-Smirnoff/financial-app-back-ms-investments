package com.financialapp.investments.domain;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.event.Direction;
import com.financialapp.investments.domain.event.HoldingClosedEvent;
import com.financialapp.investments.domain.event.HoldingCreatedEvent;
import com.financialapp.investments.domain.event.HoldingUpdatedEvent;
import com.financialapp.investments.domain.event.PriceThresholdBreachedEvent;
import com.financialapp.investments.domain.model.holding.BanksAccountId;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventsTest {

    private static final UserId USER = new UserId(1L);
    private static final HoldingId HID = new HoldingId(10L);
    private static final Ticker TIC = new Ticker("AAPL");
    private static final BanksAccountId ACC = new BanksAccountId(100L);
    private static final Money ARS_100 = Money.of(new BigDecimal("100"), "ARS");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    @Test
    void holdingCreatedEvent_exposesAccessors() {
        HoldingCreatedEvent e = new HoldingCreatedEvent(
                HID, USER, TIC, AssetType.STOCK, ACC, ACC,
                new HoldingQuantity(BigDecimal.ONE), ARS_100, ARS_100, NOW);
        assertThat(e.holdingId()).isEqualTo(HID);
        assertThat(e.userId()).isEqualTo(USER);
        assertThat(e.ticker()).isEqualTo(TIC);
        assertThat(e.assetType()).isEqualTo(AssetType.STOCK);
        assertThat(e.bankAccountId()).isEqualTo(ACC);
        assertThat(e.fundingAccountId()).isEqualTo(ACC);
        assertThat(e.totalCost()).isEqualTo(ARS_100);
        assertThat(e.occurredAt()).isEqualTo(NOW);
    }

    @Test
    void holdingCreatedEvent_acceptsNullFundingAccount() {
        HoldingCreatedEvent e = new HoldingCreatedEvent(
                HID, USER, TIC, AssetType.STOCK, ACC, null,
                new HoldingQuantity(BigDecimal.ONE), ARS_100, ARS_100, NOW);
        assertThat(e.fundingAccountId()).isNull();
    }

    @Test
    void holdingUpdatedEvent_exposesAccessors() {
        HoldingUpdatedEvent e = new HoldingUpdatedEvent(
                HID, USER, TIC, ACC, ACC,
                new HoldingQuantity(new BigDecimal("2")),
                new HoldingQuantity(new BigDecimal("1")),
                ARS_100, ARS_100, NOW);
        assertThat(e.newQuantity().value()).isEqualByComparingTo("2");
        assertThat(e.previousQuantity().value()).isEqualByComparingTo("1");
        assertThat(e.costDifference()).isEqualTo(ARS_100);
        assertThat(e.fundingAccountId()).isEqualTo(ACC);
    }

    @Test
    void holdingClosedEvent_exposesAccessors() {
        HoldingClosedEvent e = new HoldingClosedEvent(
                HID, USER, TIC, ACC, ACC, ARS_100, NOW);
        assertThat(e.holdingId()).isEqualTo(HID);
        assertThat(e.depositAccountId()).isEqualTo(ACC);
        assertThat(e.proceedsAmount()).isEqualTo(ARS_100);
        assertThat(e.occurredAt()).isEqualTo(NOW);
    }

    @Test
    void priceThresholdBreachedEvent_exposesAccessors() {
        PriceThresholdBreachedEvent e = new PriceThresholdBreachedEvent(
                HID, USER, TIC, "Apple Inc", Direction.GAIN,
                new BigDecimal("10"), new BigDecimal("12"),
                ARS_100, ARS_100, NOW);
        assertThat(e.holdingName()).isEqualTo("Apple Inc");
        assertThat(e.direction()).isEqualTo(Direction.GAIN);
        assertThat(e.thresholdPct()).isEqualByComparingTo("10");
        assertThat(e.actualPct()).isEqualByComparingTo("12");
        assertThat(e.currentPrice()).isEqualTo(ARS_100);
        assertThat(e.avgPurchasePrice()).isEqualTo(ARS_100);
    }
}
