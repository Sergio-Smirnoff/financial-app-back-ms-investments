package com.financialapp.investments.infrastructure.scheduler;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.repository.FxRateRepository;
import com.financialapp.investments.domain.service.FxRateDerivation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FxRateSyncScheduler {

    private final FxRateDerivation fxRateDerivation;
    private final FxRateRepository fxRateRepository;

    @Scheduled(cron = "${iol.price-refresh-cron}")
    public void syncFxRates() {
        LocalDate today = LocalDate.now();
        log.info("Starting scheduled FX rate sync for date={}", today);

        List<FxRate> rates = fxRateDerivation.deriveAsFxRates(today);
        for (FxRate rate : rates) {
            fxRateRepository.save(rate);
            log.info("Saved FX rate for view={} buy={} sell={}", rate.view(), rate.buy(), rate.sell());
        }
        log.info("FX rate sync completed for date={}, total rates={}", today, rates.size());
    }
}
