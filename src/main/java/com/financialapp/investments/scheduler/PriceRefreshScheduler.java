package com.financialapp.investments.scheduler;

import com.financialapp.investments.kafka.event.InvestmentThresholdEvent;
import com.financialapp.investments.kafka.producer.InvestmentEventProducer;
import com.financialapp.investments.model.entity.AssetPrice;
import com.financialapp.investments.model.entity.Holding;
import com.financialapp.investments.repository.AssetPriceRepository;
import com.financialapp.investments.repository.HoldingRepository;
import com.financialapp.investments.service.PriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceRefreshScheduler {

    private static final long NOTIFICATION_COOLDOWN_HOURS = 24;

    private final HoldingRepository holdingRepository;
    private final AssetPriceRepository assetPriceRepository;
    private final PriceService priceService;
    private final InvestmentEventProducer investmentEventProducer;

    @CacheEvict(value = "portfolio", allEntries = true)
    @Scheduled(cron = "${iol.price-refresh-cron}")
    public void refreshPrices() {
        // Fix 1b: fetch only distinct tickers — no need to load all holding data for price refresh
        List<String> distinctTickers = holdingRepository.findDistinctTickers();
        log.info("Refreshing prices for {} distinct tickers", distinctTickers.size());

        // Fix 2: parallelize per-ticker IOL HTTP calls
        List<CompletableFuture<Void>> futures = distinctTickers.stream()
                .map(ticker -> CompletableFuture.runAsync(() -> {
                    try {
                        // PriceService.fetchAndUpsertPrice needs assetType + currency;
                        // those are not available from a tickers-only query.
                        // We keep the single-holding lookup inside PriceService but
                        // fire all tickers concurrently to saturate I/O in parallel.
                        priceService.fetchAndUpsertPrice(ticker);
                    } catch (Exception ex) {
                        log.error("Failed to refresh price for ticker={}: {}", ticker, ex.getMessage());
                    }
                }))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("Price refresh completed");

        checkThresholds();
    }

    private void checkThresholds() {
        // Fix 1a: only load holdings that actually have thresholds set
        List<Holding> holdingsWithThresholds = holdingRepository.findHoldingsWithThresholds();

        if (holdingsWithThresholds.isEmpty()) return;

        // Fix 1c: batch-fetch all needed prices in one query
        List<String> tickers = holdingsWithThresholds.stream()
                .map(Holding::getTicker)
                .distinct()
                .toList();
        Map<String, AssetPrice> priceMap = assetPriceRepository.findAllByTickerIn(tickers)
                .stream()
                .collect(Collectors.toMap(AssetPrice::getTicker, p -> p));

        LocalDateTime cooldownCutoff = LocalDateTime.now().minusHours(NOTIFICATION_COOLDOWN_HOURS);

        // Fix 1d: collect modified holdings and batch-save once after the loop
        List<Holding> modifiedHoldings = new ArrayList<>();

        for (Holding h : holdingsWithThresholds) {
            AssetPrice price = priceMap.get(h.getTicker());
            if (price == null) continue;

            BigDecimal costBasis = h.getAvgPurchasePrice().multiply(h.getQuantity());
            if (costBasis.compareTo(BigDecimal.ZERO) == 0) continue;

            BigDecimal currentValue = price.getLastPrice().multiply(h.getQuantity());
            BigDecimal plPercent = currentValue.subtract(costBasis)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(costBasis, 2, RoundingMode.HALF_UP);

            boolean modified = false;

            // Check gain threshold
            if (h.getNotifyGainThresholdPct() != null
                    && plPercent.compareTo(h.getNotifyGainThresholdPct()) >= 0
                    && (h.getLastGainNotifiedAt() == null || h.getLastGainNotifiedAt().isBefore(cooldownCutoff))) {
                publishThreshold(h, price.getLastPrice(), "GAIN", h.getNotifyGainThresholdPct(), plPercent);
                h.setLastGainNotifiedAt(LocalDateTime.now());
                modified = true;
            }

            // Check loss threshold
            if (h.getNotifyLossThresholdPct() != null
                    && plPercent.negate().compareTo(h.getNotifyLossThresholdPct()) >= 0
                    && (h.getLastLossNotifiedAt() == null || h.getLastLossNotifiedAt().isBefore(cooldownCutoff))) {
                publishThreshold(h, price.getLastPrice(), "LOSS", h.getNotifyLossThresholdPct(), plPercent);
                h.setLastLossNotifiedAt(LocalDateTime.now());
                modified = true;
            }

            if (modified) {
                modifiedHoldings.add(h);
            }
        }

        if (!modifiedHoldings.isEmpty()) {
            holdingRepository.saveAll(modifiedHoldings);
        }
    }

    private void publishThreshold(Holding h, BigDecimal currentPrice, String direction,
                                  BigDecimal thresholdPct, BigDecimal actualPct) {
        InvestmentThresholdEvent event = InvestmentThresholdEvent.builder()
                .userId(h.getUserId())
                .payload(InvestmentThresholdEvent.Payload.builder()
                        .holdingId(h.getId())
                        .ticker(h.getTicker())
                        .name(h.getName())
                        .direction(direction)
                        .thresholdPct(thresholdPct)
                        .actualPct(actualPct)
                        .currentPrice(currentPrice)
                        .avgPurchasePrice(h.getAvgPurchasePrice())
                        .currency(h.getCurrency())
                        .build())
                .build();
        investmentEventProducer.publishThresholdReached(event);
    }
}
