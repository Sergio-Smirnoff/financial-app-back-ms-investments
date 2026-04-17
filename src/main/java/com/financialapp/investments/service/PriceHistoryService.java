package com.financialapp.investments.service;

import com.financialapp.investments.model.dto.internal.PriceDetail;
import com.financialapp.investments.model.entity.AssetPriceHistory;
import com.financialapp.investments.model.enums.AssetType;
import com.financialapp.investments.repository.AssetPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private final AssetPriceHistoryRepository repository;

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

    public List<AssetPriceHistory> getHistory(String ticker, LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return repository.findByTickerAndPricedAtBetweenOrderByPricedAtAsc(ticker, from, to);
        }
        return repository.findByTickerOrderByPricedAtAsc(ticker);
    }
}
