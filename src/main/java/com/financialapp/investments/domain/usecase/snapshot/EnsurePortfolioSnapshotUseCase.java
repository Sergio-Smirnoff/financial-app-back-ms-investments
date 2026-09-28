package com.financialapp.investments.domain.usecase.snapshot;

import com.financialapp.investments.domain.usecase.snapshot.command.EnsurePortfolioSnapshotCommand;
import com.financialapp.investments.domain.usecase.snapshot.response.EnsureSnapshotResult;

public interface EnsurePortfolioSnapshotUseCase {

    EnsureSnapshotResult execute(EnsurePortfolioSnapshotCommand command);
}
