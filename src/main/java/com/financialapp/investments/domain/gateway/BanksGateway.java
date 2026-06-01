package com.financialapp.investments.domain.gateway;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.model.holding.BanksAccountId;

public interface BanksGateway {

    void adjustBalance(BanksAccountId accountId, Money amount);
}
