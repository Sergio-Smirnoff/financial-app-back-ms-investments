package com.financialapp.investments.infrastructure.persistence.mapper;

import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.infrastructure.persistence.entity.AssetPriceJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AssetPricePersistenceMapper {

    public AssetPrice toDomain(AssetPriceJpaEntity e) {
        return new AssetPrice(
                new AssetPriceId(e.getId()),
                new Ticker(e.getTicker()),
                e.getAssetType(),
                e.getLastPrice(),
                e.getCurrency(),
                e.getOpenPrice(),
                e.getHighPrice(),
                e.getLowPrice(),
                e.getVolume(),
                e.getDailyVariation(),
                e.getPricedAt(),
                e.getUpdatedAt()
        );
    }

    public AssetPriceJpaEntity toEntity(AssetPrice p) {
        return AssetPriceJpaEntity.builder()
                .id(p.id() != null ? p.id().value() : null)
                .ticker(p.ticker().value())
                .assetType(p.assetType())
                .lastPrice(p.lastPrice())
                .currency(p.currency())
                .openPrice(p.openPrice())
                .highPrice(p.highPrice())
                .lowPrice(p.lowPrice())
                .volume(p.volume())
                .dailyVariation(p.dailyVariation())
                .pricedAt(p.pricedAt())
                .updatedAt(p.updatedAt())
                .build();
    }
}
