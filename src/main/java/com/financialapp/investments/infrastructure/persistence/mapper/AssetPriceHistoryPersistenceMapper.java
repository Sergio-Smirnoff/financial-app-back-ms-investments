package com.financialapp.investments.infrastructure.persistence.mapper;

import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.model.history.AssetPriceHistoryId;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.infrastructure.persistence.entity.AssetPriceHistoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AssetPriceHistoryPersistenceMapper {

    public AssetPriceHistory toDomain(AssetPriceHistoryJpaEntity e) {
        return new AssetPriceHistory(
                new AssetPriceHistoryId(e.getId()),
                new Ticker(e.getTicker()),
                e.getAssetType(),
                e.getLastPrice(),
                e.getOpenPrice(),
                e.getHighPrice(),
                e.getLowPrice(),
                e.getVolume(),
                e.getDailyVariation(),
                e.getCurrency(),
                e.getPricedAt()
        );
    }

    public AssetPriceHistoryJpaEntity toEntity(AssetPriceHistory h) {
        return AssetPriceHistoryJpaEntity.builder()
                .id(h.id() != null ? h.id().value() : null)
                .ticker(h.ticker().value())
                .assetType(h.assetType())
                .lastPrice(h.lastPrice())
                .openPrice(h.openPrice())
                .highPrice(h.highPrice())
                .lowPrice(h.lowPrice())
                .volume(h.volume())
                .dailyVariation(h.dailyVariation())
                .currency(h.currency())
                .pricedAt(h.pricedAt())
                .build();
    }
}
