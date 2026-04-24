package com.financialapp.investments.service;

import com.financialapp.investments.client.IolApiClient;
import com.financialapp.investments.model.dto.internal.MarketQuote;
import com.financialapp.investments.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDiscoveryService {

    private final IolApiClient iolApiClient;
    private final HoldingRepository holdingRepository;

    @Cacheable(value = "marketDiscovery", key = "#userId")
    public List<MarketQuote> getTrendingOpportunities(Long userId, int limit) {
        log.info("Generating trending opportunities for userId={} (limit={})", userId, limit);
        
        // 1. Fetch from Lideres panel (most active stocks in Argentina)
        List<MarketQuote> allQuotes = iolApiClient.getPanelQuotes("Lideres");
        
        if (allQuotes.isEmpty()) {
            log.warn("No quotes found in Lideres panel, trying Cedears as fallback...");
            allQuotes = iolApiClient.getPanelQuotes("Cedears");
        }

        log.info("Total quotes fetched from IOL: {}", allQuotes.size());

        // 2. Get tickers user already owns
        Set<String> ownedTickers = holdingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(h -> h.getTicker().toUpperCase().trim())
                .collect(Collectors.toSet());
        
        log.info("User {} already owns {} tickers", userId, ownedTickers.size());

        // 3. Filter out owned and sort by absolute variation (trending)
        List<MarketQuote> opportunities = allQuotes.stream()
                .filter(q -> !ownedTickers.contains(q.ticker().toUpperCase().trim()))
                .filter(q -> q.variation() != null)
                .sorted((a, b) -> b.variation().abs().compareTo(a.variation().abs()))
                .limit(limit)
                .toList();

        log.info("Returning {} trending opportunities", opportunities.size());
        return opportunities;
    }
}
