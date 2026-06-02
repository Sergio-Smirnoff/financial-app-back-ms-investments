package com.financialapp.investments.domain.gateway;

import com.financialapp.investments.domain.common.model.Cbu;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;

public interface FinancesGateway {

    void recordPurchase(UserId userId, Cbu fundingCbu, Money totalCost);

    void recordSaleProceeds(UserId userId, Cbu destinationCbu, Money proceeds);
}
