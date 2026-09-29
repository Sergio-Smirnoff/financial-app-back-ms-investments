package com.financialapp.investments.domain.usecase.snapshot;

import com.financialapp.investments.domain.usecase.snapshot.response.SnapshotCaptureResult;

public interface CapturePortfolioSnapshotUseCase {

    SnapshotCaptureResult execute();
}
