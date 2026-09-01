package com.financialapp.investments.application.fx.impl;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxView;
import com.financialapp.investments.domain.repository.FxRateRepository;
import com.financialapp.investments.domain.usecase.fx.GetLatestFxRates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetLatestFxRatesUseCaseImpl implements GetLatestFxRates {

    private final FxRateRepository fxRateRepository;

    @Override
    public List<FxRate> execute() {
        List<FxRate> result = new ArrayList<>();
        for (FxView view : FxView.values()) {
            Optional<FxRate> latest = fxRateRepository.findLatest(view);
            latest.ifPresent(result::add);
        }
        return result;
    }
}
