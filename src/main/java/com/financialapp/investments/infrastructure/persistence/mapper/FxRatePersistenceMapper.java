package com.financialapp.investments.infrastructure.persistence.mapper;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxRateId;
import com.financialapp.investments.infrastructure.persistence.entity.FxRateJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class FxRatePersistenceMapper {

    public FxRateJpaEntity toEntity(FxRate domain) {
        if (domain == null) {
            return null;
        }
        return FxRateJpaEntity.builder()
                .id(domain.id() != null ? domain.id().value() : null)
                .rateDate(domain.date())
                .fxView(domain.view())
                .buy(domain.buy())
                .sell(domain.sell())
                .source(domain.source())
                .createdAt(domain.createdAt())
                .build();
    }

    public FxRate toDomain(FxRateJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FxRate(
                new FxRateId(entity.getId()),
                entity.getRateDate(),
                entity.getFxView(),
                entity.getBuy(),
                entity.getSell(),
                entity.getSource(),
                entity.getCreatedAt()
        );
    }
}
