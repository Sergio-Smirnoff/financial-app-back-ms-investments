package com.financialapp.investments.application.fx.impl;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.repository.FxRateRepository;
import com.financialapp.investments.domain.usecase.fx.GetFxRates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFxRatesUseCaseImpl implements GetFxRates {

    private final FxRateRepository fxRateRepository;

    @Override
    public List<FxRate> execute(GetFxRatesCommand command) {
        if (command == null) {
            return List.of();
        }
        return fxRateRepository.findByDateBetween(command.from(), command.to(), command.view());
    }
}
