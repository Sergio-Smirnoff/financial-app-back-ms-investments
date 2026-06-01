package com.financialapp.investments.domain.usecase.holding;

import com.financialapp.investments.domain.usecase.holding.command.CloseHoldingCommand;

public interface CloseHoldingUseCase {

    void execute(CloseHoldingCommand command);
}
