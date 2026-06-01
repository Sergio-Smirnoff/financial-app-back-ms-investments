package com.financialapp.investments.application.holding.impl;

import com.financialapp.investments.domain.usecase.holding.command.ListHoldingsCommand;
import com.financialapp.investments.domain.usecase.holding.ListHoldingsUseCase;
import com.financialapp.investments.domain.common.model.PageResult;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.HoldingFilter;
import com.financialapp.investments.domain.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListHoldingsUseCaseImpl implements ListHoldingsUseCase {

    private final HoldingRepository holdingRepository;

    @Override
    public PageResult<Holding> execute(ListHoldingsCommand command) {
        HoldingFilter filter = new HoldingFilter(command.userId(), command.assetType());
        return holdingRepository.findFiltered(filter, command.pageRequest());
    }
}
