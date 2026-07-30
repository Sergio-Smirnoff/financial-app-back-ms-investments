package com.financialapp.investments.domain.usecase.fx;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxView;

import java.time.LocalDate;
import java.util.List;

public interface GetFxRates {
    record GetFxRatesCommand(LocalDate from, LocalDate to, FxView view) {}
    List<FxRate> execute(GetFxRatesCommand command);
}
