package com.financialapp.investments.infrastructure.persistence.mapper;

import com.financialapp.investments.domain.model.market.MarketIndex;
import com.financialapp.investments.infrastructure.persistence.entity.MarketIndexJpaEntity;

public final class MarketIndexPersistenceMapper {

    private MarketIndexPersistenceMapper() {}

    public static MarketIndex toDomain(MarketIndexJpaEntity entity) {
        if (entity == null) return null;
        return new MarketIndex(
                entity.getCode(),
                entity.getValue(),
                entity.getVariation(),
                entity.getUpdatedAt()
        );
    }

    public static MarketIndexJpaEntity toEntity(MarketIndex domain) {
        if (domain == null) return null;
        return new MarketIndexJpaEntity(
                domain.code(),
                domain.value(),
                domain.variation(),
                domain.updatedAt()
        );
    }
}
