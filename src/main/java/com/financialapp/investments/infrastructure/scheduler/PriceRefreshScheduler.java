package com.financialapp.investments.infrastructure.scheduler;

import com.financialapp.investments.domain.usecase.price.EvaluateThresholdsUseCase;
import com.financialapp.investments.domain.usecase.price.RefreshPricesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceRefreshScheduler {

    private final RefreshPricesUseCase refreshPricesUseCase;
    private final EvaluateThresholdsUseCase evaluateThresholdsUseCase;

    @CacheEvict(value = "portfolio", allEntries = true)
    @Scheduled(cron = "${iol.price-refresh-cron}")
    public void refreshPrices() {
        log.info("Starting scheduled price refresh");
        refreshPricesUseCase.execute();
        log.info("Price refresh completed — evaluating thresholds");
        evaluateThresholdsUseCase.execute();
    }
}
