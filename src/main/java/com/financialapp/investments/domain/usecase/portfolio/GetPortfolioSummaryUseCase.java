package com.financialapp.investments.domain.usecase.portfolio;

import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioSummaryCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioSummaryResult;

public interface GetPortfolioSummaryUseCase {

    PortfolioSummaryResult execute(GetPortfolioSummaryCommand command);
}
