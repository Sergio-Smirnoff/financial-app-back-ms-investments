package com.financialapp.investments.service;

import com.financialapp.investments.client.IolApiClient;
import com.financialapp.investments.model.entity.AssetPrice;
import com.financialapp.investments.model.enums.AssetType;
import com.financialapp.investments.repository.AssetPriceRepository;
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
