package com.financialapp.investments.domain;

import com.financialapp.investments.domain.common.model.Cbu;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.model.holding.NotificationTimestamps;
import com.financialapp.investments.domain.model.holding.ThresholdConfig;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoldingModelTest {

    private static final UserId USER = new UserId(1L);
    private static final Cbu ACC = new Cbu("0070009000000000000010");
    private static final Ticker TIC = new Ticker("AAPL");
    private static final HoldingQuantity QTY = new HoldingQuantity(BigDecimal.ONE);
    private static final Money PRICE = Money.of(new BigDecimal("100"), "ARS");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    private Holding sample() {
        return new Holding(new HoldingId(1L), USER, ACC, TIC, "Apple",
                AssetType.STOCK, QTY, PRICE, ThresholdConfig.disabled(),
                NotificationTimestamps.empty(), NOW, NOW);
    }

    @Test
    void blankName_throws() {
        assertThatThrownBy(() -> new Holding(null, USER, ACC, TIC, "   ",
                AssetType.STOCK, QTY, PRICE, ThresholdConfig.disabled(),
                NotificationTimestamps.empty(), NOW, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullUserId_throws() {
        assertThatThrownBy(() -> new Holding(null, null, ACC, TIC, "n",
                AssetType.STOCK, QTY, PRICE, ThresholdConfig.disabled(),
                NotificationTimestamps.empty(), NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullName_throws() {
        assertThatThrownBy(() -> new Holding(null, USER, ACC, TIC, null,
                AssetType.STOCK, QTY, PRICE, ThresholdConfig.disabled(),
                NotificationTimestamps.empty(), NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullAssetType_throws() {
        assertThatThrownBy(() -> new Holding(null, USER, ACC, TIC, "n",
                null, QTY, PRICE, ThresholdConfig.disabled(),
                NotificationTimestamps.empty(), NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullQuantity_throws() {
        assertThatThrownBy(() -> new Holding(null, USER, ACC, TIC, "n",
                AssetType.STOCK, null, PRICE, ThresholdConfig.disabled(),
                NotificationTimestamps.empty(), NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullPrice_throws() {
        assertThatThrownBy(() -> new Holding(null, USER, ACC, TIC, "n",
                AssetType.STOCK, QTY, null, ThresholdConfig.disabled(),
                NotificationTimestamps.empty(), NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullTicker_throws() {
        assertThatThrownBy(() -> new Holding(null, USER, ACC, null, "n",
                AssetType.STOCK, QTY, PRICE, ThresholdConfig.disabled(),
                NotificationTimestamps.empty(), NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullNotificationTimestamps_throws() {
        assertThatThrownBy(() -> new Holding(null, USER, ACC, TIC, "n",
                AssetType.STOCK, QTY, PRICE, ThresholdConfig.disabled(),
                null, NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void withNotificationTimestamps_replacesField_keepsIdentity() {
        Holding h = sample();
        NotificationTimestamps ts = NotificationTimestamps.empty().withGainNotifiedAt(NOW);
        Holding updated = h.withNotificationTimestamps(ts);
        assertThat(updated.notificationTimestamps()).isEqualTo(ts);
        assertThat(updated.id()).isEqualTo(h.id());
        assertThat(updated.ticker()).isEqualTo(h.ticker());
    }

    @Test
    void notificationTimestamps_emptyAndWithGainAndWithLoss() {
        NotificationTimestamps ts = NotificationTimestamps.empty();
        assertThat(ts.lastGainNotifiedAt()).isNull();
        assertThat(ts.lastLossNotifiedAt()).isNull();
        NotificationTimestamps gained = ts.withGainNotifiedAt(NOW);
        assertThat(gained.lastGainNotifiedAt()).isEqualTo(NOW);
        assertThat(gained.lastLossNotifiedAt()).isNull();
        NotificationTimestamps lossed = gained.withLossNotifiedAt(NOW);
        assertThat(lossed.lastLossNotifiedAt()).isEqualTo(NOW);
        assertThat(lossed.lastGainNotifiedAt()).isEqualTo(NOW);
    }
}
