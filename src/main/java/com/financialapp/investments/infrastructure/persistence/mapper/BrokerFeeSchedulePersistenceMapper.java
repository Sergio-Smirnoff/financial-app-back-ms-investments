package com.financialapp.investments.infrastructure.persistence.mapper;

import com.financialapp.commons.core.domain.model.IvaTreatment;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.fee.BrokerFeeSchedule;
import com.financialapp.investments.domain.model.fee.BrokerFeeScheduleId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.infrastructure.persistence.entity.BrokerFeeScheduleJpaEntity;

public final class BrokerFeeSchedulePersistenceMapper {

    private BrokerFeeSchedulePersistenceMapper() {}

    public static BrokerFeeSchedule toDomain(BrokerFeeScheduleJpaEntity entity) {
        if (entity == null) return null;

        Money minFee = entity.getMinimumFee() != null
                ? Money.of(entity.getMinimumFee(), entity.getCurrency() != null ? entity.getCurrency() : "ARS")
                : null;

        AssetType assetType = entity.getAssetType() != null && !entity.getAssetType().isBlank()
                ? AssetType.valueOf(entity.getAssetType())
                : null;

        IvaTreatment ivaTreatment = entity.getIvaTreatment() != null
                ? IvaTreatment.valueOf(entity.getIvaTreatment())
                : IvaTreatment.SEPARATE;

        return new BrokerFeeSchedule(
                entity.getId() != null ? new BrokerFeeScheduleId(entity.getId()) : null,
                new BankNumber(entity.getBankNumber()),
                assetType,
                entity.getBuyFeePct(),
                entity.getSellFeePct(),
                minFee,
                entity.getMarketFeePct(),
                ivaTreatment
        );
    }

    public static BrokerFeeScheduleJpaEntity toEntity(BrokerFeeSchedule domain) {
        if (domain == null) return null;

        String currency = domain.minimumFee() != null ? domain.minimumFee().currency().getCurrencyCode() : "ARS";

        return new BrokerFeeScheduleJpaEntity(
                domain.id() != null ? domain.id().value() : null,
                domain.bankNumber().value(),
                domain.assetType() != null ? domain.assetType().name() : null,
                domain.buyFeePct(),
                domain.sellFeePct(),
                domain.minimumFee() != null ? domain.minimumFee().amount() : null,
                domain.marketFeePct(),
                domain.ivaTreatment() != null ? domain.ivaTreatment().name() : IvaTreatment.SEPARATE.name(),
                currency
        );
    }
}
