package com.financialapp.investments.infrastructure.persistence.mapper;

import com.financialapp.investments.domain.common.model.Cbu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.model.history.AssetPriceHistoryId;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.market.MarketQuote;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.model.refresh.RefreshJob;
import com.financialapp.investments.domain.model.refresh.RefreshJobId;
import com.financialapp.investments.domain.model.refresh.RefreshJobStatus;
import com.financialapp.investments.infrastructure.persistence.entity.AssetPriceHistoryJpaEntity;
import com.financialapp.investments.infrastructure.persistence.entity.AssetPriceJpaEntity;
import com.financialapp.investments.infrastructure.persistence.entity.HoldingJpaEntity;
import com.financialapp.investments.infrastructure.persistence.entity.MarketPanelQuoteJpaEntity;
import com.financialapp.investments.infrastructure.persistence.entity.RefreshJobJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistenceMappersTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    // -------- Holding --------

    @Test
    void holdingMapper_roundTrip() {
        HoldingPersistenceMapper m = new HoldingPersistenceMapper();
        Holding original = new Holding(new HoldingId(1L), new UserId(7L),
                new Cbu("0070009000000000000017"), new Ticker("AAPL"),
                "Apple", AssetType.STOCK, new HoldingQuantity(BigDecimal.ONE),
                Money.of(new BigDecimal("100"), "ARS"),
                new ThresholdConfig(BigDecimal.ONE, BigDecimal.ONE),
                new NotificationTimestamps(NOW, NOW), NOW, NOW);

        HoldingJpaEntity entity = m.toEntity(original);
        assertThat(entity.getCurrency()).isEqualTo("ARS");
        assertThat(entity.getAccountCbu()).isEqualTo("0070009000000000000017");

        Holding back = m.toDomain(entity);
        assertThat(back.id().value()).isEqualTo(1L);
        assertThat(back.accountCbu().value()).isEqualTo("0070009000000000000017");
        assertThat(back.avgPurchasePrice().currency().getCurrencyCode()).isEqualTo("ARS");
        assertThat(back.thresholdConfig().gainPct()).isEqualByComparingTo("1");
        assertThat(back.notificationTimestamps().lastGainNotifiedAt()).isEqualTo(NOW);
    }

    @Test
    void holdingMapper_handlesNullOptionalFields() {
        HoldingPersistenceMapper m = new HoldingPersistenceMapper();
        Holding original = new Holding(null, new UserId(7L), null, new Ticker("AAPL"),
                "Apple", AssetType.STOCK, new HoldingQuantity(BigDecimal.ONE),
                Money.of(new BigDecimal("100"), "ARS"), null,
                NotificationTimestamps.empty(), NOW, NOW);
        HoldingJpaEntity entity = m.toEntity(original);
        assertThat(entity.getId()).isNull();
        assertThat(entity.getAccountCbu()).isNull();
        assertThat(entity.getNotifyGainThresholdPct()).isNull();
        Holding back = m.toDomain(entity);
        assertThat(back.accountCbu()).isNull();
    }

    // -------- AssetPrice --------

    @Test
    void assetPriceMapper_roundTrip() {
        AssetPricePersistenceMapper m = new AssetPricePersistenceMapper();
        AssetPrice ap = new AssetPrice(new AssetPriceId(1L), new Ticker("AAPL"),
                AssetType.STOCK, new BigDecimal("100"), "ARS",
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, NOW, NOW);
        AssetPriceJpaEntity e = m.toEntity(ap);
        assertThat(e.getId()).isEqualTo(1L);
        assertThat(e.getCurrency()).isEqualTo("ARS");
        AssetPrice back = m.toDomain(e);
        assertThat(back.ticker().value()).isEqualTo("AAPL");
        assertThat(back.lastPrice()).isEqualByComparingTo("100");
    }

    @Test
    void assetPriceMapper_nullId() {
        AssetPricePersistenceMapper m = new AssetPricePersistenceMapper();
        AssetPrice ap = new AssetPrice(null, new Ticker("AAPL"), AssetType.STOCK,
                BigDecimal.ONE, "ARS", null, null, null, null, null, NOW, NOW);
        assertThat(m.toEntity(ap).getId()).isNull();
    }

    // -------- AssetPriceHistory --------

    @Test
    void assetPriceHistoryMapper_roundTrip() {
        AssetPriceHistoryPersistenceMapper m = new AssetPriceHistoryPersistenceMapper();
        AssetPriceHistory h = new AssetPriceHistory(new AssetPriceHistoryId(1L),
                new Ticker("AAPL"), AssetType.STOCK, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, "ARS", NOW);
        AssetPriceHistoryJpaEntity e = m.toEntity(h);
        assertThat(e.getCurrency()).isEqualTo("ARS");
        AssetPriceHistory back = m.toDomain(e);
        assertThat(back.ticker().value()).isEqualTo("AAPL");
        assertThat(back.lastPrice()).isEqualByComparingTo("1");
    }

    @Test
    void assetPriceHistoryMapper_nullId() {
        AssetPriceHistoryPersistenceMapper m = new AssetPriceHistoryPersistenceMapper();
        AssetPriceHistory h = new AssetPriceHistory(null, new Ticker("AAPL"), AssetType.STOCK,
                BigDecimal.ONE, null, null, null, null, null, "ARS", NOW);
        assertThat(m.toEntity(h).getId()).isNull();
    }

    // -------- MarketQuote --------

    @Test
    void marketQuoteMapper_roundTrip_explicitCurrency() {
        MarketQuotePersistenceMapper m = new MarketQuotePersistenceMapper();
        MarketQuote q = new MarketQuote(new Ticker("X"), Money.of(BigDecimal.ONE, "USD"),
                BigDecimal.ONE, BigDecimal.ONE, NOW);
        MarketPanelQuoteJpaEntity e = m.toEntity(q);
        assertThat(e.getCurrency()).isEqualTo("USD");
        MarketQuote back = m.toDomain(e);
        assertThat(back.price().currency().getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void marketQuoteMapper_toDomain_nullCurrency_defaultsToArs() {
        MarketQuotePersistenceMapper m = new MarketQuotePersistenceMapper();
        MarketPanelQuoteJpaEntity e = MarketPanelQuoteJpaEntity.builder()
                .ticker("X").lastPrice(BigDecimal.ONE).currency(null)
                .variation(BigDecimal.ONE).volume(BigDecimal.ONE).updatedAt(NOW).build();
        MarketQuote q = m.toDomain(e);
        assertThat(q.price().currency().getCurrencyCode()).isEqualTo("ARS");
    }

    @Test
    void marketQuoteMapper_toEntity_nullUpdatedAt_defaultsToNow() {
        MarketQuotePersistenceMapper m = new MarketQuotePersistenceMapper();
        MarketQuote q = new MarketQuote(new Ticker("X"), Money.of(BigDecimal.ONE, "ARS"),
                BigDecimal.ONE, BigDecimal.ONE, null);
        assertThat(m.toEntity(q).getUpdatedAt()).isNotNull();
    }

    // -------- RefreshJob --------

    @Test
    void refreshJobMapper_roundTrip() {
        RefreshJobPersistenceMapper m = new RefreshJobPersistenceMapper(new ObjectMapper());
        RefreshJob j = new RefreshJob(new RefreshJobId(1L), RefreshJobStatus.IN_PROGRESS,
                List.of("AAPL", "GOOG"), 0, null, NOW, null, NOW);
        RefreshJobJpaEntity e = m.toEntity(j);
        assertThat(e.getAllTickers()).contains("AAPL").contains("GOOG");
        RefreshJob back = m.toDomain(e);
        assertThat(back.id().value()).isEqualTo(1L);
        assertThat(back.allTickers()).containsExactly("AAPL", "GOOG");
        assertThat(back.status()).isEqualTo(RefreshJobStatus.IN_PROGRESS);
    }

    @Test
    void refreshJobMapper_nullId() {
        RefreshJobPersistenceMapper m = new RefreshJobPersistenceMapper(new ObjectMapper());
        RefreshJob j = new RefreshJob(null, RefreshJobStatus.PENDING, List.of("X"), -1, null, NOW, null, NOW);
        assertThat(m.toEntity(j).getId()).isNull();
    }

    @Test
    void refreshJobMapper_deserializeBadJson_throws() {
        ObjectMapper om = new ObjectMapper();
        RefreshJobPersistenceMapper m = new RefreshJobPersistenceMapper(om);
        RefreshJobJpaEntity e = RefreshJobJpaEntity.builder()
                .id(1L).status(RefreshJobStatus.PENDING).allTickers("{not-json").lastSuccessIndex(0)
                .startedAt(NOW).updatedAt(NOW).build();
        assertThatThrownBy(() -> m.toDomain(e)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refreshJobMapper_serializeFailure_throws() throws Exception {
        ObjectMapper broken = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws com.fasterxml.jackson.core.JsonProcessingException {
                throw new com.fasterxml.jackson.core.JsonProcessingException("forced") {};
            }
        };
        RefreshJobPersistenceMapper m = new RefreshJobPersistenceMapper(broken);
        RefreshJob j = new RefreshJob(new RefreshJobId(1L), RefreshJobStatus.PENDING,
                List.of("X"), -1, null, NOW, null, NOW);
        assertThatThrownBy(() -> m.toEntity(j)).isInstanceOf(IllegalStateException.class);
    }
}
