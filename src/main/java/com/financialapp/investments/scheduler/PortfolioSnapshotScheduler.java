package com.financialapp.investments.scheduler;

import com.financialapp.investments.model.entity.PortfolioSnapshot;
import com.financialapp.investments.repository.HoldingRepository;
import com.financialapp.investments.repository.PortfolioSnapshotRepository;
import com.financialapp.investments.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioSnapshotScheduler {
    private final HoldingRepository holdingRepository;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final PortfolioService portfolioService;

    @Scheduled(cron = "0 0 0 * * *") // Midnight
    @Transactional
    public void captureSnapshots() {
        log.info("Starting daily portfolio snapshot capture...");
        List<Long> userIds = holdingRepository.findDistinctUserIds();
        
        for (Long userId : userIds) {
            try {
                var summary = portfolioService.getSummary(userId);
                snapshotRepository.save(PortfolioSnapshot.builder()
                        .userId(userId)
                        .snapshotDate(LocalDate.now())
                        .totalValueArs(summary.getTotalValueArs())
                        .totalValueUsd(summary.getTotalValueUsd())
                        .build());
            } catch (Exception e) {
                log.error("Failed to capture snapshot for user {}: {}", userId, e.getMessage());
            }
        }
    }
}
