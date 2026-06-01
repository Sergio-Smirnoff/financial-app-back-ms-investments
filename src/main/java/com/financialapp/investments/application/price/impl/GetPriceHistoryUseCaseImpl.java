package com.financialapp.investments.application.price.impl;

import com.financialapp.investments.domain.gateway.HoldingQueryGateway;
import com.financialapp.investments.domain.usecase.price.command.GetPriceHistoryCommand;
import com.financialapp.investments.domain.usecase.price.GetPriceHistoryUseCase;
import com.financialapp.investments.domain.exception.IolServiceException;
import com.financialapp.investments.infrastructure.exception.InfrastructureException;
import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.model.history.AssetPriceHistoryId;
import com.financialapp.investments.domain.model.history.HistoricalPricePoint;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.gateway.IolGateway;
import com.financialapp.investments.domain.repository.AssetPriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GetPriceHistoryUseCaseImpl implements GetPriceHistoryUseCase {

    private static final int BACKFILL_THRESHOLD = 3;

    private final AssetPriceHistoryRepository historyRepository;
    private final HoldingQueryGateway holdingQueryGateway;
    private final IolGateway iolGateway;

    @Override
    public List<AssetPriceHistory> execute(GetPriceHistoryCommand command) {
        long count = historyRepository.countByTickerAndPricedAtBetween(
                command.ticker(), command.from(), command.to());

        if (count < BACKFILL_THRESHOLD) {
            backfill(command.ticker(), command.from(), command.to());
        }

        return historyRepository.findByTickerAndPricedAtBetween(
                command.ticker(), command.from(), command.to());
    }

    private void backfill(Ticker ticker, LocalDateTime from, LocalDateTime to) {
        AssetType assetType = holdingQueryGateway.findFirstByTicker(ticker)
                .map(Holding::assetType)
                .orElse(AssetType.STOCK);

        List<HistoricalPricePoint> points;
        try {
            points = iolGateway.getHistoricalSeries(ticker, assetType,
                    from.toLocalDate(), to.toLocalDate());
        } catch (InfrastructureException e) {
            throw new IolServiceException(
                    "Failed to backfill price history for: " + ticker.value(), e);
        }

        for (HistoricalPricePoint point : points) {
            if (!historyRepository.existsByTickerAndPricedAt(ticker, point.pricedAt())) {
                historyRepository.save(new AssetPriceHistory(
                        new AssetPriceHistoryId(null), ticker, assetType,
                        point.lastPrice(), point.openPrice(), point.highPrice(),
                        point.lowPrice(), point.volume(), point.dailyVariation(),
                        point.currency(), point.pricedAt()
                ));
            }
        }
    }
}
