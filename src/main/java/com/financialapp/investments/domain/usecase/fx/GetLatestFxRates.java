package com.financialapp.investments.domain.usecase.fx;

import com.financialapp.investments.domain.model.fx.FxRate;

import java.util.List;

public interface GetLatestFxRates {
    List<FxRate> execute();
}
