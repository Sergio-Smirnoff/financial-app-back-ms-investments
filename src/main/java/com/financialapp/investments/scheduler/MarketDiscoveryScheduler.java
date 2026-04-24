package com.financialapp.investments.scheduler;

import com.financialapp.investments.client.IolApiClient;
import com.financialapp.investments.model.dto.internal.MarketQuote;
import com.financialapp.investments.model.entity.MarketPanelQuote;
import com.financialapp.investments.repository.MarketPanelQuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketDiscoveryScheduler {

    private final IolApiClient iolApiClient;
    private final MarketPanelQuoteRepository quoteRepository;

    @Scheduled(fixedRateString = "${iol.discovery-refresh-rate:900000}") // 15 minutes default
    public void syncMarketQuotes() {
        log.info("Starting scheduled sync of market panel quotes...");
        
        List<MarketQuote> quotes = iolApiClient.getPanelQuotes("Lideres");
        if (quotes.isEmpty()) {
            log.warn("No quotes in Lideres panel, trying Cedears...");
            quotes = iolApiClient.getPanelQuotes("Cedears");
        }

        if (quotes.isEmpty()) {
            log.warn("Failed to fetch market quotes from IOL. Skipping database update to preserve existing cache.");
            return;
        }

        List<MarketPanelQuote> entities = quotes.stream()
                .map(q -> MarketPanelQuote.builder()
                        .ticker(q.ticker().toUpperCase().trim())
                        .lastPrice(q.price())
                        .variation(q.variation() != null ? q.variation() : java.math.BigDecimal.ZERO)
                        .build())
                .toList();

        quoteRepository.saveAll(entities);
        log.info("Successfully synced {} market quotes to the database.", entities.size());
    }
}
