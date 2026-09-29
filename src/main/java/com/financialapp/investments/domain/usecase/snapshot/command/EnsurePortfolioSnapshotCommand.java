package com.financialapp.investments.domain.usecase.snapshot.command;

import com.financialapp.investments.domain.common.model.UserId;

public record EnsurePortfolioSnapshotCommand(UserId userId) {}
