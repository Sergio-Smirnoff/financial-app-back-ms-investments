package com.financialapp.investments.domain.usecase.holding.command;

import com.financialapp.investments.domain.common.model.PageRequest;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.price.AssetType;

public record ListHoldingsCommand(
        UserId userId,
        AssetType assetType,
        PageRequest pageRequest
) {}
