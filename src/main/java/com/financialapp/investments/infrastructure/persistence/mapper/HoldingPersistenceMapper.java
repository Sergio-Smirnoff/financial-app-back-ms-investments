package com.financialapp.investments.infrastructure.persistence.mapper;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.infrastructure.persistence.entity.HoldingJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class HoldingPersistenceMapper {

    public Holding toDomain(HoldingJpaEntity e) {
        return new Holding(
                new HoldingId(e.getId()),
                new UserId(e.getUserId()),
                e.getBankAccountId() != null ? new BanksAccountId(e.getBankAccountId()) : null,
                e.getBankId() != null ? new BankId(e.getBankId()) : null,
                new Ticker(e.getTicker()),
                e.getName(),
                e.getAssetType(),
                new HoldingQuantity(e.getQuantity()),
                new Money(e.getAvgPurchasePrice(), Currency.getInstance(e.getCurrency())),
                new ThresholdConfig(e.getNotifyGainThresholdPct(), e.getNotifyLossThresholdPct()),
                new NotificationTimestamps(e.getLastGainNotifiedAt(), e.getLastLossNotifiedAt()),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public HoldingJpaEntity toEntity(Holding h) {
        return HoldingJpaEntity.builder()
                .id(h.id() != null ? h.id().value() : null)
                .userId(h.userId().value())
                .bankAccountId(h.bankAccountId() != null ? h.bankAccountId().value() : null)
                .bankId(h.bankId() != null ? h.bankId().value() : null)
                .ticker(h.ticker().value())
                .name(h.name())
                .assetType(h.assetType())
                .quantity(h.quantity().value())
                .avgPurchasePrice(h.avgPurchasePrice().amount())
                .currency(h.avgPurchasePrice().currency().getCurrencyCode())
                .notifyGainThresholdPct(h.thresholdConfig() != null ? h.thresholdConfig().gainPct() : null)
                .notifyLossThresholdPct(h.thresholdConfig() != null ? h.thresholdConfig().lossPct() : null)
                .lastGainNotifiedAt(h.notificationTimestamps().lastGainNotifiedAt())
                .lastLossNotifiedAt(h.notificationTimestamps().lastLossNotifiedAt())
                .createdAt(h.createdAt())
                .updatedAt(h.updatedAt())
                .build();
    }
}
