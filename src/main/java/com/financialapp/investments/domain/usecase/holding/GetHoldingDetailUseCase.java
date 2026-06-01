package com.financialapp.investments.domain.usecase.holding;

import com.financialapp.investments.domain.usecase.holding.command.GetHoldingDetailCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.HoldingWithPriceResult;

public interface GetHoldingDetailUseCase {

    HoldingWithPriceResult execute(GetHoldingDetailCommand command);
}
