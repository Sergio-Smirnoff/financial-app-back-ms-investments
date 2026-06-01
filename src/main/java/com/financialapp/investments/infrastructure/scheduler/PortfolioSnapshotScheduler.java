package com.financialapp.investments.infrastructure.scheduler;

import com.financialapp.investments.domain.usecase.snapshot.CapturePortfolioSnapshotUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioSnapshotScheduler {

    private final CapturePortfolioSnapshotUseCase capturePortfolioSnapshotUseCase;

    @Scheduled(cron = "0 0 0 * * *")
    public void captureSnapshots() {
        log.info("Starting daily portfolio snapshot capture");
        capturePortfolioSnapshotUseCase.execute();
        log.info("Portfolio snapshot capture completed");
    }
}
