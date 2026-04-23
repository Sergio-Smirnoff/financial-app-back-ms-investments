package com.financialapp.investments.service;

import com.financialapp.investments.model.dto.response.AllocationBreakdown;
import com.financialapp.investments.model.dto.response.HoldingWithPriceResponse;
import com.financialapp.investments.model.dto.response.PortfolioSummaryResponse;
import com.financialapp.investments.model.entity.AssetPrice;
import com.financialapp.investments.model.entity.Holding;
import com.financialapp.investments.repository.AssetPriceRepository;
import com.financialapp.investments.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;

    @Transactional(readOnly = true)
    public List<HoldingWithPriceResponse> getHoldingsWithPrices(Long userId) {
        List<Holding> holdings = holdingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<String> tickers = holdings.stream().map(Holding::getTicker).distinct().toList();
        Map<String, AssetPrice> priceMap = assetPriceRepository.findAllByTickerIn(tickers)
                .stream()
                .collect(Collectors.toMap(AssetPrice::getTicker, p -> p));

        return holdings.stream().map(h -> {
            AssetPrice price = priceMap.get(h.getTicker());
            BigDecimal currentPrice = price != null ? price.getLastPrice() : null;
            BigDecimal currentValue = null;
            BigDecimal plAmount = null;
            BigDecimal plPercent = null;

            if (currentPrice != null) {
                currentValue = currentPrice.multiply(h.getQuantity());
                BigDecimal costBasis = h.getAvgPurchasePrice().multiply(h.getQuantity());
                plAmount = currentValue.subtract(costBasis);
                if (costBasis.compareTo(BigDecimal.ZERO) != 0) {
                    plPercent = plAmount.multiply(BigDecimal.valueOf(100))
                            .divide(costBasis, 2, RoundingMode.HALF_UP);
                }
            }

            return HoldingWithPriceResponse.builder()
                    .id(h.getId())
                    .userId(h.getUserId())
                    .bankId(h.getBankId())
                    .bankAccountId(h.getBankAccountId())
                    .ticker(h.getTicker())
                    .name(h.getName())
                    .assetType(h.getAssetType().name())
                    .quantity(h.getQuantity())
                    .avgPurchasePrice(h.getAvgPurchasePrice())
                    .currency(h.getCurrency())
                    .notifyGainThresholdPct(h.getNotifyGainThresholdPct())
                    .notifyLossThresholdPct(h.getNotifyLossThresholdPct())
                    .lastGainNotifiedAt(h.getLastGainNotifiedAt())
                    .lastLossNotifiedAt(h.getLastLossNotifiedAt())
                    .currentPrice(currentPrice)
                    .currentValue(currentValue)
                    .plAmount(plAmount)
                    .plPercent(plPercent)
                    .createdAt(h.getCreatedAt())
                    .updatedAt(h.getUpdatedAt())
                    .build();
        }).toList();
    }

    @Cacheable(value = "portfolio", key = "#userId")
    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getSummary(Long userId) {
        List<HoldingWithPriceResponse> enriched = getHoldingsWithPrices(userId);

        BigDecimal totalValueArs = BigDecimal.ZERO;
        BigDecimal totalValueUsd = BigDecimal.ZERO;
        BigDecimal totalCostArs = BigDecimal.ZERO;
        BigDecimal totalCostUsd = BigDecimal.ZERO;

        // Group by currency and asset type for allocation breakdown
        Map<String, List<HoldingWithPriceResponse>> byCurrency = enriched.stream()
                .collect(Collectors.groupingBy(HoldingWithPriceResponse::getCurrency));

        for (HoldingWithPriceResponse h : enriched) {
            if (h.getCurrentValue() == null) continue;
            BigDecimal costBasis = h.getAvgPurchasePrice().multiply(h.getQuantity());
            if ("ARS".equals(h.getCurrency())) {
                totalValueArs = totalValueArs.add(h.getCurrentValue());
                totalCostArs = totalCostArs.add(costBasis);
            } else {
                totalValueUsd = totalValueUsd.add(h.getCurrentValue());
                totalCostUsd = totalCostUsd.add(costBasis);
            }
        }

        BigDecimal totalPlArs = totalValueArs.subtract(totalCostArs);
        BigDecimal totalPlUsd = totalValueUsd.subtract(totalCostUsd);
        BigDecimal plPercentArs = totalCostArs.compareTo(BigDecimal.ZERO) != 0
                ? totalPlArs.multiply(BigDecimal.valueOf(100)).divide(totalCostArs, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal plPercentUsd = totalCostUsd.compareTo(BigDecimal.ZERO) != 0
                ? totalPlUsd.multiply(BigDecimal.valueOf(100)).divide(totalCostUsd, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<AllocationBreakdown> breakdownArs = buildBreakdown(byCurrency.getOrDefault("ARS", List.of()), totalValueArs, "ARS");
        List<AllocationBreakdown> breakdownUsd = buildBreakdown(byCurrency.getOrDefault("USD", List.of()), totalValueUsd, "USD");

        return PortfolioSummaryResponse.builder()
                .totalValueArs(totalValueArs)
                .totalValueUsd(totalValueUsd)
                .totalPlArs(totalPlArs)
                .totalPlUsd(totalPlUsd)
                .plPercentArs(plPercentArs)
                .plPercentUsd(plPercentUsd)
                .breakdownArs(breakdownArs)
                .breakdownUsd(breakdownUsd)
                .build();
    }

    private List<AllocationBreakdown> buildBreakdown(List<HoldingWithPriceResponse> holdings,
                                                      BigDecimal totalValue, String currency) {
        if (totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return List.of();
        }

        Map<String, BigDecimal> byType = holdings.stream()
                .filter(h -> h.getCurrentValue() != null)
                .collect(Collectors.groupingBy(
                        HoldingWithPriceResponse::getAssetType,
                        Collectors.reducing(BigDecimal.ZERO,
                                HoldingWithPriceResponse::getCurrentValue,
                                BigDecimal::add)));

        List<AllocationBreakdown> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : byType.entrySet()) {
            BigDecimal percentage = entry.getValue()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalValue, 2, RoundingMode.HALF_UP);
            result.add(AllocationBreakdown.builder()
                    .assetType(entry.getKey())
                    .totalValue(entry.getValue())
                    .currency(currency)
                    .percentage(percentage)
                    .build());
        }
        return result;
    }
}
