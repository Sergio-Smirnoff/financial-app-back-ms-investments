package com.financialapp.investments.domain.usecase.portfolio;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.Holding;

import java.util.List;

public interface SearchPositions {
    List<Holding> execute(UserId userId, String query);
}
