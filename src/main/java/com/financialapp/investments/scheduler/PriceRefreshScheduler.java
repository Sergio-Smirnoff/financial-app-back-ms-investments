package com.financialapp.investments.scheduler;

import com.financialapp.investments.model.entity.Holding;
import com.financialapp.investments.repository.HoldingRepository;
import com.financialapp.investments.service.PriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceRefreshScheduler {

    private final HoldingRepository holdingRepository;
    private final PriceService priceService;

    @Scheduled(cron = "${iol.price-refresh-cron}")
    public void refreshPrices() {
        // Build a map of ticker -> first holding (to get assetType and currency)
        List<Holding> allHoldings = holdingRepository.findAll();
        Map<String, Holding> tickerMap = allHoldings.stream()
                .collect(Collectors.toMap(
                        Holding::getTicker,
                        h -> h,
                        (existing, replacement) -> existing));

        log.info("Refreshing prices for {} distinct tickers", tickerMap.size());

        tickerMap.forEach((ticker, holding) -> {
            try {
                priceService.fetchAndUpsertPrice(ticker, holding.getAssetType(), holding.getCurrency());
            } catch (Exception ex) {
                log.error("Failed to refresh price for ticker={}: {}", ticker, ex.getMessage());
            }
        });

        log.info("Price refresh completed");
    }
}
