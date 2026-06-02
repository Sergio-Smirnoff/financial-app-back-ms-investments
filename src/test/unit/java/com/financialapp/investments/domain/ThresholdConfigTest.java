package com.financialapp.investments.domain;

import com.financialapp.investments.domain.model.holding.ThresholdConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class ThresholdConfigTest {

    @Test
    void negativeGainPct_throws() {
        assertThatThrownBy(() -> new ThresholdConfig(new BigDecimal("-1"), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeLossPct_throws() {
        assertThatThrownBy(() -> new ThresholdConfig(null, new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void zeroPct_accepted() {
        assertThatNoException().isThrownBy(() -> new ThresholdConfig(BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void nullBoth_disabled() {
        ThresholdConfig cfg = ThresholdConfig.disabled();
        assertThat(cfg.hasGainThreshold()).isFalse();
        assertThat(cfg.hasLossThreshold()).isFalse();
    }

    @Test
    void onlyGain_configured() {
        ThresholdConfig cfg = new ThresholdConfig(new BigDecimal("10"), null);
        assertThat(cfg.hasGainThreshold()).isTrue();
        assertThat(cfg.hasLossThreshold()).isFalse();
    }

    @Test
    void onlyLoss_configured() {
        ThresholdConfig cfg = new ThresholdConfig(null, new BigDecimal("5"));
        assertThat(cfg.hasGainThreshold()).isFalse();
        assertThat(cfg.hasLossThreshold()).isTrue();
    }
}
