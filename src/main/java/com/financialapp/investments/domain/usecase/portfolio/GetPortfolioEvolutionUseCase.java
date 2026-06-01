package com.financialapp.investments.domain.usecase.portfolio;

import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioEvolutionCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioEvolutionPoint;

import java.util.List;

public interface GetPortfolioEvolutionUseCase {

    List<PortfolioEvolutionPoint> execute(GetPortfolioEvolutionCommand command);
}
