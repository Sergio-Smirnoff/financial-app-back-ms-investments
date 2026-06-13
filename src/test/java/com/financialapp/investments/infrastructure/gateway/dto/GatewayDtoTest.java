package com.financialapp.investments.infrastructure.gateway.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayDtoTest {

    @Test
    void iolPriceDetail_accessors() {
        IolPriceDetail d = new IolPriceDetail(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, "USD");
        assertThat(d.lastPrice()).isEqualByComparingTo("1");
        assertThat(d.openPrice()).isEqualByComparingTo("1");
        assertThat(d.highPrice()).isEqualByComparingTo("1");
        assertThat(d.lowPrice()).isEqualByComparingTo("1");
        assertThat(d.volume()).isEqualByComparingTo("1");
        assertThat(d.dailyVariation()).isEqualByComparingTo("1");
        assertThat(d.currency()).isEqualTo("USD");
    }

    @Test
    void iolMarketQuote_accessors() {
        IolMarketQuote q = new IolMarketQuote("X", BigDecimal.TEN, BigDecimal.ONE, "ARS");
        assertThat(q.ticker()).isEqualTo("X");
        assertThat(q.price()).isEqualByComparingTo("10");
        assertThat(q.variation()).isEqualByComparingTo("1");
        assertThat(q.currency()).isEqualTo("ARS");
    }

    @Test
    void iolHistoricalPricePoint_accessors() {
        LocalDateTime at = LocalDateTime.of(2026, 1, 1, 0, 0);
        IolPriceDetail d = new IolPriceDetail(BigDecimal.ONE, null, null, null, null, null, "ARS");
        IolHistoricalPricePoint p = new IolHistoricalPricePoint(at, d);
        assertThat(p.pricedAt()).isEqualTo(at);
        assertThat(p.detail()).isSameAs(d);
    }
}
