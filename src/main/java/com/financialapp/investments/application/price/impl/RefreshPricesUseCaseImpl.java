package com.financialapp.investments.application.price.impl;

import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.usecase.price.RefreshPricesUseCase;
import com.financialapp.investments.domain.exception.IolServiceException;
import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.model.history.AssetPriceHistoryId;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetPrice;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.model.price.PriceDetail;
import com.financialapp.investments.domain.model.refresh.RefreshJob;
import com.financialapp.investments.domain.gateway.IolGateway;
import com.financialapp.investments.domain.repository.AssetPriceHistoryRepository;
import com.financialapp.investments.domain.repository.AssetPriceRepository;
import com.financialapp.investments.domain.repository.RefreshJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshPricesUseCaseImpl implements RefreshPricesUseCase {

    private final HoldingQueryGateway holdingQueryGateway;
    private final AssetPriceRepository assetPriceRepository;
    private final AssetPriceHistoryRepository assetPriceHistoryRepository;
    private final RefreshJobRepository refreshJobRepository;
    private final IolGateway iolGateway;

    @Override
    public void execute() {
        List<String> tickerStrings = holdingQueryGateway.findDistinctTickers()
                .stream()
                .map(Ticker::value)
                .toList();

        if (tickerStrings.isEmpty()) {
            return;
        }

        RefreshJob job = refreshJobRepository.save(RefreshJob.start(tickerStrings));

        for (int i = job.lastSuccessIndex() + 1; i < job.allTickers().size(); i++) {
            String tickerValue = job.allTickers().get(i);
            Ticker ticker = new Ticker(tickerValue);
            AssetType assetType = resolveAssetType(ticker);

            try {
                Optional<PriceDetail> priceOpt = iolGateway.getPrice(ticker, assetType);
                if (priceOpt.isEmpty()) {
                    continue;
                }

                PriceDetail detail = priceOpt.get();
                LocalDateTime now = LocalDateTime.now();

                Optional<AssetPrice> maybeAssetPrice = Optional.ofNullable(assetPriceRepository.findByTicker(ticker).orElse(null));
                assetPriceRepository.save(new AssetPrice(
                        maybeAssetPrice.isPresent() ? maybeAssetPrice.get().id() : new AssetPriceId(null),
                        ticker, assetType,
                        detail.lastPrice(), detail.currency(),
                        detail.openPrice(), detail.highPrice(), detail.lowPrice(),
                        detail.volume(), detail.dailyVariation(),
                        now, now
                ));

                assetPriceHistoryRepository.save(new AssetPriceHistory(
                        new AssetPriceHistoryId(null), ticker, assetType,
                        detail.lastPrice(), detail.openPrice(), detail.highPrice(),
                        detail.lowPrice(), detail.volume(), detail.dailyVariation(),
                        detail.currency(), now
                ));

                job = refreshJobRepository.save(job.advance(i));

            } catch (IolServiceException e) {
                refreshJobRepository.save(job.interrupt(e.getMessage()));
                throw new IolServiceException(
                        "Price refresh interrupted at ticker: " + tickerValue, e);
            }
        }

        refreshJobRepository.save(job.complete());
    }

    private AssetType resolveAssetType(Ticker ticker) {
        return holdingQueryGateway.findFirstByTicker(ticker)
                .map(Holding::assetType)
                .orElse(AssetType.STOCK);
    }
}
