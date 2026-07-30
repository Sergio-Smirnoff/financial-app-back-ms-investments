package com.financialapp.investments.domain.usecase.fx;

import java.time.LocalDate;

public interface BackfillFxRates {

    record BackfillFxRatesCommand(LocalDate from, LocalDate to) {}
    record BackfillResult(int createdCount) {}

    BackfillResult execute(BackfillFxRatesCommand command);
}
