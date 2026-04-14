package com.financialapp.investments.service;

import com.financialapp.investments.client.IolApiClient;
import com.financialapp.investments.model.entity.AssetPrice;
import com.financialapp.investments.model.entity.Holding;
import com.financialapp.investments.model.enums.AssetType;
import com.financialapp.investments.repository.AssetPriceRepository;
import com.financialapp.investments.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceService {

    private final IolApiClient iolApiClient;
    private final AssetPriceRepository assetPriceRepository;
    private final HoldingRepository holdingRepository;

    /**
     * Fetch and upsert price for a ticker. Resolves assetType and currency from the
     * existing AssetPrice record (if any) or from the first holding with that ticker.
     * Called by the scheduler with only a ticker so that the scheduler can work from
     * {@link HoldingRepository#findDistinctTickers()} without loading full Holding data.
     */
    @Transactional
    public void fetchAndUpsertPrice(String ticker) {
        // Prefer metadata from an already-persisted AssetPrice; fall back to a holding
        Optional<AssetPrice> existing = assetPriceRepository.findByTicker(ticker);
        AssetType assetType;
        String currency;

        if (existing.isPresent()) {
            assetType = existing.get().getAssetType();
            currency = existing.get().getCurrency();
        } else {
            Optional<Holding> anyHolding = holdingRepository.findFirstByTicker(ticker);
            if (anyHolding.isEmpty()) {
                log.warn("No holding found for ticker={}, skipping price refresh", ticker);
                return;
            }
            assetType = anyHolding.get().getAssetType();
            currency = anyHolding.get().getCurrency();
        }

        fetchAndUpsertPrice(ticker, assetType, currency);
    }

    /**
     * Explicit overload used when assetType and currency are already known (e.g. from a
     * freshly-created holding).
     */
    @Transactional
    public void fetchAndUpsertPrice(String ticker, AssetType assetType, String currency) {
        Optional<BigDecimal> price = iolApiClient.getPrice(ticker, assetType);

        price.ifPresent(lastPrice -> {
            AssetPrice assetPrice = assetPriceRepository.findByTicker(ticker)
                    .orElse(AssetPrice.builder()
                            .ticker(ticker)
                            .assetType(assetType)
                            .currency(currency)
                            .build());

            assetPrice.setLastPrice(lastPrice);
            assetPrice.setPricedAt(LocalDateTime.now());
            assetPriceRepository.save(assetPrice);
            log.debug("Updated price for ticker={}: {}", ticker, lastPrice);
        });
    }
}
