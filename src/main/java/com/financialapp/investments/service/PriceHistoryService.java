package com.financialapp.investments.service;

import com.financialapp.investments.client.IolApiClient;
import com.financialapp.investments.model.dto.internal.HistoricalPricePoint;
import com.financialapp.investments.model.dto.internal.PriceDetail;
import com.financialapp.investments.model.entity.AssetPriceHistory;
import com.financialapp.investments.model.entity.Holding;
import com.financialapp.investments.model.enums.AssetType;
import com.financialapp.investments.repository.AssetPriceHistoryRepository;
import com.financialapp.investments.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceHistoryService {

    private final AssetPriceHistoryRepository repository;
    private final IolApiClient iolApiClient;
    private final HoldingRepository holdingRepository;

    @Transactional
    public void saveSnapshot(String ticker, AssetType assetType, String currency, PriceDetail detail) {
        repository.save(AssetPriceHistory.builder()
                .ticker(ticker)
                .assetType(assetType)
                .lastPrice(detail.lastPrice())
                .openPrice(detail.openPrice())
                .highPrice(detail.highPrice())
                .lowPrice(detail.lowPrice())
                .volume(detail.volume())
                .dailyVariation(detail.dailyVariation())
                .currency(currency)
                .pricedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public List<AssetPriceHistory> getHistory(String ticker, LocalDateTime from, LocalDateTime to) {
        LocalDateTime startOfToday = LocalDate.now().atTime(LocalTime.MIDNIGHT);
        LocalDateTime threeDaysAgo = startOfToday.minusDays(3);
        long previousDaysCount = repository.countByTickerAndPricedAtBetween(ticker, threeDaysAgo, startOfToday);

        if (previousDaysCount < 3) {
            backfillFromIol(ticker);
        }

        if (from != null && to != null) {
            return repository.findByTickerAndPricedAtBetweenOrderByPricedAtAsc(ticker, from, to);
        }
        return repository.findByTickerOrderByPricedAtAsc(ticker);
    }

    private void backfillFromIol(String ticker) {
        Optional<Holding> holdingOpt = holdingRepository.findFirstByTicker(ticker);
        if (holdingOpt.isEmpty()) {
            log.warn("Backfill skipped — no holding found for ticker={}", ticker);
            return;
        }
        Holding holding = holdingOpt.get();
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(3);

        log.info("Backfilling price history for ticker={} from={} to={}", ticker, from, to);
        List<HistoricalPricePoint> points = iolApiClient.getHistoricalSeries(ticker, holding.getAssetType(), from, to);

        for (HistoricalPricePoint point : points) {
            boolean exists = !repository.findByTickerAndPricedAtBetweenOrderByPricedAtAsc(
                    ticker, point.pricedAt().minusMinutes(30), point.pricedAt().plusMinutes(30)).isEmpty();
            if (!exists) {
                repository.save(AssetPriceHistory.builder()
                        .ticker(ticker)
                        .assetType(holding.getAssetType())
                        .lastPrice(point.detail().lastPrice())
                        .openPrice(point.detail().openPrice())
                        .highPrice(point.detail().highPrice())
                        .lowPrice(point.detail().lowPrice())
                        .volume(point.detail().volume())
                        .dailyVariation(point.detail().dailyVariation())
                        .currency(holding.getCurrency())
                        .pricedAt(point.pricedAt())
                        .build());
            }
        }
    }
}
