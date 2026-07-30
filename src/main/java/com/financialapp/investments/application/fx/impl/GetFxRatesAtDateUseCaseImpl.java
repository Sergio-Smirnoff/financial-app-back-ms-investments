package com.financialapp.investments.application.fx.impl;

import com.financialapp.investments.domain.model.fx.FxSnapshotRates;
import com.financialapp.investments.domain.service.FxRateDerivation;
import com.financialapp.investments.domain.usecase.fx.GetFxRatesAtDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFxRatesAtDateUseCaseImpl implements GetFxRatesAtDate {

    private final FxRateDerivation fxRateDerivation;

    @Override
    public FxSnapshotRates execute(GetFxRatesAtDateCommand command) {
        if (command == null || command.date() == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        if (command.date().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Future dates are not allowed");
        }
        return fxRateDerivation.deriveRates(command.date());
    }
}
