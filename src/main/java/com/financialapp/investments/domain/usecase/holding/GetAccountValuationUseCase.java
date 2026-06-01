package com.financialapp.investments.domain.usecase.holding;

import com.financialapp.investments.domain.usecase.holding.command.GetAccountValuationCommand;
import com.financialapp.investments.domain.usecase.holding.response.AccountValuationResult;

public interface GetAccountValuationUseCase {

    AccountValuationResult execute(GetAccountValuationCommand command);
}
