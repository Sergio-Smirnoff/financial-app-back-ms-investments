package com.financialapp.investments.application.price.impl;

import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.usecase.price.EvaluateThresholdsUseCase;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.event.Direction;
import com.financialapp.investments.domain.event.PriceThresholdBreachedEvent;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.NotificationTimestamps;
import com.financialapp.investments.domain.model.holding.ThresholdConfig;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.gateway.DomainEventPublisher;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EvaluateThresholdsUseCaseImpl implements EvaluateThresholdsUseCase {

    private static final int COOLDOWN_HOURS = 24;

    private final HoldingQueryGateway holdingQueryGateway;
    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final DomainEventPublisher eventPublisher;

    @Override
    public void execute() {
        List<Holding> holdings = holdingQueryGateway.findWithThresholds();

        Set<Ticker> tickers = holdings.stream()
                .map(Holding::ticker)
                .collect(Collectors.toSet());

        Map<Ticker, BigDecimal> priceMap = assetPriceRepository.findAllByTickerIn(tickers)
                .stream()
                .collect(Collectors.toMap(AssetPrice::ticker, AssetPrice::lastPrice, (a, b) -> b));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cooldownCutoff = now.minusHours(COOLDOWN_HOURS);
        List<Holding> modified = new ArrayList<>();

        for (Holding holding : holdings) {
            BigDecimal currentPrice = priceMap.get(holding.ticker());
            if (currentPrice == null) continue;

            BigDecimal avgPrice = holding.avgPurchasePrice().amount();
            if (avgPrice.compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal plPercent = currentPrice.subtract(avgPrice)
                    .divide(avgPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            ThresholdConfig config = holding.thresholdConfig();
            NotificationTimestamps timestamps = holding.notificationTimestamps();
            NotificationTimestamps updated = timestamps;

            if (config.hasGainThreshold() && plPercent.compareTo(config.gainPct()) >= 0) {
                boolean canNotify = timestamps.lastGainNotifiedAt() == null
                        || timestamps.lastGainNotifiedAt().isBefore(cooldownCutoff);
                if (canNotify) {
                    eventPublisher.publish(new PriceThresholdBreachedEvent(
                            holding.id(), holding.userId(), holding.ticker(), holding.name(),
                            Direction.GAIN, config.gainPct(), plPercent,
                            new Money(currentPrice, holding.avgPurchasePrice().currency()),
                            holding.avgPurchasePrice(), now
                    ));
                    updated = updated.withGainNotifiedAt(now);
                }
            }

            if (config.hasLossThreshold()
                    && plPercent.compareTo(config.lossPct().negate()) <= 0) {
                boolean canNotify = timestamps.lastLossNotifiedAt() == null
                        || timestamps.lastLossNotifiedAt().isBefore(cooldownCutoff);
                if (canNotify) {
                    eventPublisher.publish(new PriceThresholdBreachedEvent(
                            holding.id(), holding.userId(), holding.ticker(), holding.name(),
                            Direction.LOSS, config.lossPct(),
                            plPercent.abs(),
                            new Money(currentPrice, holding.avgPurchasePrice().currency()),
                            holding.avgPurchasePrice(), now
                    ));
                    updated = updated.withLossNotifiedAt(now);
                }
            }

            if (updated != timestamps) {
                modified.add(holding.withNotificationTimestamps(updated));
            }
        }

        if (!modified.isEmpty()) {
            holdingRepository.saveAll(modified);
        }
    }
}
