package com.financialapp.investments.domain.usecase.holding;

import com.financialapp.investments.domain.usecase.holding.command.UpdateHoldingCommand;
import com.financialapp.investments.domain.model.holding.Holding;

public interface UpdateHoldingUseCase {

    Holding execute(UpdateHoldingCommand command);
}
