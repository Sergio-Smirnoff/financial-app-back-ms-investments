package com.financialapp.investments.domain.usecase.snapshot.response;

import java.time.LocalDate;

public record EnsureSnapshotResult(boolean created, LocalDate date) {}
