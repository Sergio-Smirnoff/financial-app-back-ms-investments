package com.financialapp.investments.domain.usecase.price;

import com.financialapp.investments.domain.usecase.price.command.GetPriceHistoryCommand;
import com.financialapp.investments.domain.model.history.AssetPriceHistory;

import java.util.List;

public interface GetPriceHistoryUseCase {

    List<AssetPriceHistory> execute(GetPriceHistoryCommand command);
}
