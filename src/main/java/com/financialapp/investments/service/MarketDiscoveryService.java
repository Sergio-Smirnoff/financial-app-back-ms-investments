package com.financialapp.investments.service;

import com.financialapp.investments.client.IolApiClient;
import com.financialapp.investments.model.dto.internal.MarketQuote;
import com.financialapp.investments.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketDiscoveryService {

    private final IolApiClient iolApiClient;
    private final HoldingRepository holdingRepository;

    @Cacheable(value = "marketDiscovery", key = "#userId")
    public List<MarketQuote> getTrendingOpportunities(Long userId, int limit) {
        // 1. Fetch all from Merval panel (most common trending assets in Argentina)
        List<MarketQuote> allQuotes = iolApiClient.getPanelQuotes("Merval");
        
        // 2. Get tickers user already owns
        Set<String> ownedTickers = holdingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(h -> h.getTicker().toUpperCase())
                .collect(Collectors.toSet());
        
        // 3. Filter out owned and sort by variation (trending)
        return allQuotes.stream()
                .filter(q -> !ownedTickers.contains(q.ticker().toUpperCase()))
                .sorted((a, b) -> b.variation().abs().compareTo(a.variation().abs())) // Most volatile/trending
                .limit(limit)
                .toList();
    }
}
