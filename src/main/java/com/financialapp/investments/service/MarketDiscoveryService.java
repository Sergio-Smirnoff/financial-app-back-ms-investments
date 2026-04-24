package com.financialapp.investments.service;

import com.financialapp.investments.model.dto.internal.MarketQuote;
import com.financialapp.investments.model.entity.MarketPanelQuote;
import com.financialapp.investments.repository.HoldingRepository;
import com.financialapp.investments.repository.MarketPanelQuoteRepository;
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

    private final MarketPanelQuoteRepository quoteRepository;
    private final HoldingRepository holdingRepository;

    @Cacheable(value = "marketDiscovery", key = "#userId")
    public List<MarketQuote> getTrendingOpportunities(Long userId, int limit) {
        log.info("Generating trending opportunities for userId={} from DB cache", userId);

        List<MarketPanelQuote> dbQuotes = quoteRepository.findAll();

        if (dbQuotes.isEmpty()) {
            log.warn("No cached market quotes found in database.");
            return List.of();
        }

        Set<String> ownedTickers = holdingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(h -> h.getTicker().toUpperCase().trim())
                .collect(Collectors.toSet());

        List<MarketQuote> opportunities = dbQuotes.stream()
                .filter(q -> !ownedTickers.contains(q.getTicker().toUpperCase().trim()))
                .sorted((a, b) -> b.getVariation().abs().compareTo(a.getVariation().abs()))
                .limit(limit)
                .map(q -> new MarketQuote(q.getTicker(), q.getLastPrice(), q.getVariation()))
                .toList();

        log.info("Returning {} trending opportunities (out of {} cached quotes)", 
                opportunities.size(), dbQuotes.size());
        return opportunities;
    }
}
