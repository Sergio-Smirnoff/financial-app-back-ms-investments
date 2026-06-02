package com.financialapp.investments.web.mapper;

import com.financialapp.investments.domain.usecase.holding.response.AccountValuationResult;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.NotificationTimestamps;
import com.financialapp.investments.domain.model.holding.ThresholdConfig;
import com.financialapp.investments.web.dto.response.AccountValuationResponse;
import com.financialapp.investments.web.dto.response.HoldingDetailResponse;
import com.financialapp.investments.web.dto.response.HoldingResponse;
import com.financialapp.investments.web.dto.response.HoldingWithPriceResponse;
import org.springframework.stereotype.Component;

import static com.financialapp.investments.web.mapper.BigDecimals.toPlain;

@Component
public class HoldingWebMapper {

    public HoldingResponse toResponse(Holding holding) {
        ThresholdConfig tc = holding.thresholdConfig();
        return HoldingResponse.builder()
                .id(holding.id() != null ? holding.id().value() : null)
                .userId(holding.userId().value())
                .bankId(holding.bankId() != null ? holding.bankId().value() : null)
                .bankAccountId(holding.bankAccountId() != null ? holding.bankAccountId().value() : null)
                .ticker(holding.ticker().value())
                .name(holding.name())
                .assetType(holding.assetType().name())
                .quantity(toPlain(holding.quantity().value()))
                .avgPurchasePrice(toPlain(holding.avgPurchasePrice().amount()))
                .currency(holding.avgPurchasePrice().currency().getCurrencyCode())
                .notifyGainThresholdPct(tc != null ? toPlain(tc.gainPct()) : null)
                .notifyLossThresholdPct(tc != null ? toPlain(tc.lossPct()) : null)
                .createdAt(holding.createdAt())
                .updatedAt(holding.updatedAt())
                .build();
    }

    public HoldingWithPriceResponse toWithPriceResponse(HoldingWithPriceResult result) {
        Holding holding = result.holding();
        ThresholdConfig tc = holding.thresholdConfig();
        NotificationTimestamps ts = holding.notificationTimestamps();
        return HoldingWithPriceResponse.builder()
                .id(holding.id() != null ? holding.id().value() : null)
                .userId(holding.userId().value())
                .bankId(holding.bankId() != null ? holding.bankId().value() : null)
                .bankAccountId(holding.bankAccountId() != null ? holding.bankAccountId().value() : null)
                .ticker(holding.ticker().value())
                .name(holding.name())
                .assetType(holding.assetType().name())
                .quantity(toPlain(holding.quantity().value()))
                .avgPurchasePrice(toPlain(holding.avgPurchasePrice().amount()))
                .currency(holding.avgPurchasePrice().currency().getCurrencyCode())
                .notifyGainThresholdPct(tc != null ? toPlain(tc.gainPct()) : null)
                .notifyLossThresholdPct(tc != null ? toPlain(tc.lossPct()) : null)
                .lastGainNotifiedAt(ts != null ? ts.lastGainNotifiedAt() : null)
                .lastLossNotifiedAt(ts != null ? ts.lastLossNotifiedAt() : null)
                .createdAt(holding.createdAt())
                .updatedAt(holding.updatedAt())
                .currentPrice(toPlain(result.currentPrice()))
                .currentValue(toPlain(result.currentValue()))
                .plAmount(toPlain(result.plAmount()))
                .plPercent(toPlain(result.plPercent()))
                .build();
    }

    public HoldingDetailResponse toDetailResponse(HoldingWithPriceResult result) {
        Holding holding = result.holding();
        ThresholdConfig tc = holding.thresholdConfig();
        NotificationTimestamps ts = holding.notificationTimestamps();
        return HoldingDetailResponse.builder()
                .id(holding.id() != null ? holding.id().value() : null)
                .userId(holding.userId().value())
                .bankId(holding.bankId() != null ? holding.bankId().value() : null)
                .bankAccountId(holding.bankAccountId() != null ? holding.bankAccountId().value() : null)
                .ticker(holding.ticker().value())
                .name(holding.name())
                .assetType(holding.assetType().name())
                .quantity(toPlain(holding.quantity().value()))
                .avgPurchasePrice(toPlain(holding.avgPurchasePrice().amount()))
                .currency(holding.avgPurchasePrice().currency().getCurrencyCode())
                .notifyGainThresholdPct(tc != null ? toPlain(tc.gainPct()) : null)
                .notifyLossThresholdPct(tc != null ? toPlain(tc.lossPct()) : null)
                .lastGainNotifiedAt(ts != null ? ts.lastGainNotifiedAt() : null)
                .lastLossNotifiedAt(ts != null ? ts.lastLossNotifiedAt() : null)
                .createdAt(holding.createdAt())
                .updatedAt(holding.updatedAt())
                .currentPrice(toPlain(result.currentPrice()))
                .currentValue(toPlain(result.currentValue()))
                .plAmount(toPlain(result.plAmount()))
                .plPercent(toPlain(result.plPercent()))
                .build();
    }

    public AccountValuationResponse toValuationResponse(AccountValuationResult result) {
        return AccountValuationResponse.builder()
                .accountId(result.accountId().value())
                .totalValuation(toPlain(result.totalValuation()))
                .currency(result.currency())
                .build();
    }
}
