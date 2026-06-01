package com.financialapp.investments.domain.repository;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;

import java.time.LocalDate;
import java.util.List;

public interface PortfolioSnapshotRepository {

    PortfolioSnapshot save(PortfolioSnapshot snapshot);

    List<PortfolioSnapshot> findByUserIdAndSnapshotDateAfter(UserId userId, LocalDate from);
}
