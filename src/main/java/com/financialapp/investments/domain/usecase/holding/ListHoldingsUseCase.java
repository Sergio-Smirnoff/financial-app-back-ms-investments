package com.financialapp.investments.domain.usecase.holding;

import com.financialapp.investments.domain.usecase.holding.command.ListHoldingsCommand;
import com.financialapp.investments.domain.common.model.PageResult;
import com.financialapp.investments.domain.model.holding.Holding;

public interface ListHoldingsUseCase {

    PageResult<Holding> execute(ListHoldingsCommand command);
}
