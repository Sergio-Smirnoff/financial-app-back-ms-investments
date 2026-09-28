package com.financialapp.investments.domain.usecase.snapshot.response;

public record SnapshotCaptureResult(int attempted, int failed) {

    public SnapshotCaptureResult {
        if (attempted < 0 || failed < 0 || failed > attempted) {
            throw new IllegalArgumentException(
                    "failed must be between 0 and attempted: attempted=" + attempted + ", failed=" + failed);
        }
    }
}
