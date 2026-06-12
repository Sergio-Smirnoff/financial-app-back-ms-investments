package com.financialapp.investments.domain.gateway;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.Ticker;

import java.util.Currency;
import java.util.List;
import java.util.Optional;

public interface HoldingQueryGateway {

    List<UserId> findDistinctUserIds();

    List<Ticker> findDistinctTickers();

    List<Holding> findWithThresholds();

    Optional<Holding> findFirstByTicker(Ticker ticker);

    List<Holding> findByUserIdAndBankNumberAndCurrency(UserId userId, BankNumber bankNumber, Currency currency);
}
