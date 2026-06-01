package com.financialapp.investments.domain.usecase.holding.command;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.BanksAccountId;
import com.financialapp.investments.domain.model.holding.BankId;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.model.holding.ThresholdConfig;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetType;

public record CreateHoldingCommand(
        UserId userId,
        BanksAccountId bankAccountId,
        BankId bankId,
        Ticker ticker,
        String name,
        AssetType assetType,
        HoldingQuantity quantity,
        Money avgPurchasePrice,
        ThresholdConfig thresholdConfig,
        BanksAccountId fundingAccountId
) {}
