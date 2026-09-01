package com.financialapp.investments.domain.repository;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FxRateRepository {
    FxRate save(FxRate rate);
    Optional<FxRate> findByDate(LocalDate date, FxView view);
    List<FxRate> findByDateBetween(LocalDate from, LocalDate to, FxView view);
    Optional<FxRate> findLatest(FxView view);
}
