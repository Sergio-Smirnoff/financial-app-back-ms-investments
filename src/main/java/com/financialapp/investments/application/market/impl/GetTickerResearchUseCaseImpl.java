package com.financialapp.investments.application.market.impl;

import com.financialapp.investments.domain.gateway.IolGateway;
import com.financialapp.investments.domain.usecase.market.GetTickerResearchUseCase;
import com.financialapp.investments.domain.usecase.market.command.GetTickerResearchCommand;
import com.financialapp.investments.domain.usecase.market.response.TickerResearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class GetTickerResearchUseCaseImpl implements GetTickerResearchUseCase {

    private final IolGateway iolGateway;

    @Override
    public TickerResearchResult execute(GetTickerResearchCommand command) {
        LocalDate today = LocalDate.now();
        return new TickerResearchResult(
                command.ticker(),
                iolGateway.getPrice(command.ticker(), command.assetType()),
                iolGateway.getHistoricalSeries(command.ticker(), command.assetType(),
                        command.range().from(today), command.range().to(today)));
    }
}
