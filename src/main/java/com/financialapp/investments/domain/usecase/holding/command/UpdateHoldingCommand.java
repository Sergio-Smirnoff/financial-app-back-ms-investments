package com.financialapp.investments.domain.usecase.holding.command;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Cbu;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.model.holding.ThresholdConfig;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetType;

public record UpdateHoldingCommand(
        UserId userId,
        HoldingId holdingId,
        BankNumber bankNumber,
        Ticker ticker,
        String name,
        AssetType assetType,
        HoldingQuantity newQuantity,
        Money newAvgPurchasePrice,
        ThresholdConfig thresholdConfig,
        Cbu fundingCbu
) {}
