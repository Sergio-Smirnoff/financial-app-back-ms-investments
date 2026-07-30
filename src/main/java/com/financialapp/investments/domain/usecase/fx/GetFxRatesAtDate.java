package com.financialapp.investments.domain.usecase.fx;

import com.financialapp.investments.domain.model.fx.FxSnapshotRates;

import java.time.LocalDate;

public interface GetFxRatesAtDate {
    record GetFxRatesAtDateCommand(LocalDate date) {}
    FxSnapshotRates execute(GetFxRatesAtDateCommand command);
}
