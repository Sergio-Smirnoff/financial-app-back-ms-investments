package com.financialapp.investments.domain.usecase.portfolio;

import com.financialapp.investments.domain.usecase.portfolio.command.GetHoldingsWithPricesCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;

import java.util.List;

public interface GetHoldingsWithPricesUseCase {

    List<HoldingWithPriceResult> execute(GetHoldingsWithPricesCommand command);
}
