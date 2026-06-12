package com.financialapp.investments.domain.usecase.market.command;

import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.market.PriceRange;
import com.financialapp.investments.domain.model.price.AssetType;

public record GetTickerResearchCommand(Ticker ticker, AssetType assetType, PriceRange range) {}
