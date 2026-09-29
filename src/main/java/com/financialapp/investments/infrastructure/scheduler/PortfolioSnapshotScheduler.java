package com.financialapp.investments.infrastructure.scheduler;

import com.financialapp.investments.domain.usecase.snapshot.CapturePortfolioSnapshotUseCase;
import com.financialapp.investments.domain.usecase.snapshot.response.SnapshotCaptureResult;
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
        SnapshotCaptureResult result = capturePortfolioSnapshotUseCase.execute();
        if (result.failed() > 0) {
            log.error("Portfolio snapshot capture failed for {} of {} users", result.failed(), result.attempted());
        } else {
            log.info("Portfolio snapshot capture completed for {} users", result.attempted());
        }
    }
}
