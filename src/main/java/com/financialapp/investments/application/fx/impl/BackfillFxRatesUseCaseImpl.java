package com.financialapp.investments.application.fx.impl;

import com.financialapp.investments.domain.model.fx.*;
import com.financialapp.investments.domain.repository.FxRateRepository;
import com.financialapp.investments.domain.service.FxRateDerivation;
import com.financialapp.investments.domain.usecase.fx.BackfillFxRates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BackfillFxRatesUseCaseImpl implements BackfillFxRates {

    private final FxRateRepository fxRateRepository;
    private final FxRateDerivation fxRateDerivation;

    @Override
    public BackfillResult execute(BackfillFxRatesCommand command) {
        if (command == null || command.from() == null || command.to() == null) {
            throw new IllegalArgumentException("from and to dates must not be null");
        }
        if (command.from().isAfter(command.to())) {
            throw new IllegalArgumentException("from date must be on or before to date");
        }

        int createdCount = 0;
        LocalDate current = command.from();

        while (!current.isAfter(command.to())) {
            List<FxView> missingViews = new ArrayList<>();
            for (FxView view : FxView.values()) {
                if (fxRateRepository.findByDate(current, view).isEmpty()) {
                    missingViews.add(view);
                }
            }

            if (!missingViews.isEmpty()) {
                FxSnapshotRates snapshot = fxRateDerivation.deriveRates(current);
                for (FxView view : missingViews) {
                    BigDecimal rateVal = switch (view) {
                        case MEP -> snapshot.mepRate();
                        case CCL -> snapshot.cclRate();
                        case OFICIAL -> snapshot.oficialRate();
                    };

                    if (rateVal != null) {
                        FxRate rate = new FxRate(null, current, view, rateVal, rateVal, FxRateSource.IOL_SYNTHETIC, null);
                        fxRateRepository.save(rate);
                        createdCount++;
                    }
                }
            }
            current = current.plusDays(1);
        }

        log.info("Backfill FX rates completed from={} to={}, createdCount={}", command.from(), command.to(), createdCount);
        return new BackfillResult(createdCount);
    }
}
