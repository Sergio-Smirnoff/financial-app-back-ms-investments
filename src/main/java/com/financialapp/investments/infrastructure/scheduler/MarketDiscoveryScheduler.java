package com.financialapp.investments.infrastructure.scheduler;

import com.financialapp.investments.domain.usecase.market.SyncMarketQuotesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketDiscoveryScheduler {

    private final SyncMarketQuotesUseCase syncMarketQuotesUseCase;

    @Scheduled(fixedRateString = "${iol.discovery-refresh-rate:900000}")
    public void syncMarketQuotes() {
        log.info("Starting scheduled sync of market panel quotes");
        syncMarketQuotesUseCase.execute();
        log.info("Market panel quotes sync completed");
    }
}
