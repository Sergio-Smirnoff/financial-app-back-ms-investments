package com.financialapp.investments.domain.usecase.market;

import com.financialapp.investments.domain.usecase.market.command.GetTickerResearchCommand;
import com.financialapp.investments.domain.usecase.market.response.TickerResearchResult;

public interface GetTickerResearchUseCase {
    TickerResearchResult execute(GetTickerResearchCommand command);
}
