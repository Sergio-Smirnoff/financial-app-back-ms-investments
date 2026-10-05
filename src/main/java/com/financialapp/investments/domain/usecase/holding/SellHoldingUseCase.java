package com.financialapp.investments.domain.usecase.holding;

import com.financialapp.investments.domain.usecase.holding.command.SellHoldingCommand;
import com.financialapp.investments.domain.usecase.holding.response.HoldingSaleResult;

public interface SellHoldingUseCase {

    HoldingSaleResult execute(SellHoldingCommand command);
}
