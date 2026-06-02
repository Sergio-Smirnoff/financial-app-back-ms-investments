package com.financialapp.investments.domain;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.model.history.HistoricalPricePoint;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.model.price.PriceDetail;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceAndMarketModelTest {

    private static final Ticker TIC = new Ticker("AAPL");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final BigDecimal P = new BigDecimal("100");

    // --- AssetPrice ---

    @Test
    void assetPrice_nullLastPrice_throws() {
        assertThatThrownBy(() -> new AssetPrice(null, TIC, AssetType.STOCK,
                null, "ARS", P, P, P, P, P, NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetPrice_nullTicker_throws() {
        assertThatThrownBy(() -> new AssetPrice(null, null, AssetType.STOCK,
                P, "ARS", P, P, P, P, P, NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetPrice_nullAssetType_throws() {
        assertThatThrownBy(() -> new AssetPrice(null, TIC, null,
                P, "ARS", P, P, P, P, P, NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetPrice_nullCurrency_throws() {
        assertThatThrownBy(() -> new AssetPrice(null, TIC, AssetType.STOCK,
                P, null, P, P, P, P, P, NOW, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetPrice_valid_accessorsWork() {
        AssetPrice ap = new AssetPrice(null, TIC, AssetType.STOCK,
                P, "ARS", P, P, P, P, P, NOW, NOW);
        assertThat(ap.ticker()).isEqualTo(TIC);
        assertThat(ap.currency()).isEqualTo("ARS");
        assertThat(ap.pricedAt()).isEqualTo(NOW);
    }

    // --- PriceDetail ---

    @Test
    void priceDetail_nullLastPrice_throws() {
        assertThatThrownBy(() -> new PriceDetail(null, P, P, P, P, P, "ARS"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void priceDetail_nullCurrency_throws() {
        assertThatThrownBy(() -> new PriceDetail(P, P, P, P, P, P, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void priceDetail_valid() {
        PriceDetail d = new PriceDetail(P, P, P, P, P, P, "USD");
        assertThat(d.currency()).isEqualTo("USD");
    }

    // --- MarketQuote ---

    @Test
    void marketQuote_nullTicker_throws() {
        assertThatThrownBy(() -> new MarketQuote(null, Money.of(P, "ARS"), P, P, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void marketQuote_nullPrice_throws() {
        assertThatThrownBy(() -> new MarketQuote(TIC, null, P, P, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void marketQuote_valid() {
        MarketQuote q = new MarketQuote(TIC, Money.of(P, "ARS"), P, P, NOW);
        assertThat(q.ticker()).isEqualTo(TIC);
        assertThat(q.updatedAt()).isEqualTo(NOW);
    }

    // --- HistoricalPricePoint ---

    @Test
    void historicalPricePoint_nullLastPrice_throws() {
        assertThatThrownBy(() -> new HistoricalPricePoint(null, P, P, P, P, P, "ARS", NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void historicalPricePoint_nullPricedAt_throws() {
        assertThatThrownBy(() -> new HistoricalPricePoint(P, P, P, P, P, P, "ARS", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void historicalPricePoint_valid() {
        HistoricalPricePoint h = new HistoricalPricePoint(P, P, P, P, P, P, "ARS", NOW);
        assertThat(h.currency()).isEqualTo("ARS");
    }

    // --- AssetPriceHistory ---

    @Test
    void assetPriceHistory_nullTicker_throws() {
        assertThatThrownBy(() -> new AssetPriceHistory(null, null, AssetType.STOCK,
                P, P, P, P, P, P, "ARS", NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetPriceHistory_nullAssetType_throws() {
        assertThatThrownBy(() -> new AssetPriceHistory(null, TIC, null,
                P, P, P, P, P, P, "ARS", NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetPriceHistory_nullLastPrice_throws() {
        assertThatThrownBy(() -> new AssetPriceHistory(null, TIC, AssetType.STOCK,
                null, P, P, P, P, P, "ARS", NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetPriceHistory_nullPricedAt_throws() {
        assertThatThrownBy(() -> new AssetPriceHistory(null, TIC, AssetType.STOCK,
                P, P, P, P, P, P, "ARS", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void assetPriceHistory_valid() {
        AssetPriceHistory h = new AssetPriceHistory(null, TIC, AssetType.STOCK,
                P, P, P, P, P, P, "ARS", NOW);
        assertThat(h.ticker()).isEqualTo(TIC);
    }
}
