package com.financialapp.investments.infrastructure.gateway.impl;

import com.financialapp.investments.infrastructure.gateway.BanksClient;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.infrastructure.exception.InfrastructureException;
import com.financialapp.investments.domain.model.holding.BanksAccountId;
import com.financialapp.investments.domain.gateway.BanksGateway;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BanksGatewayImpl implements BanksGateway {

    private final BanksClient banksClient;

    @Override
    public void adjustBalance(BanksAccountId accountId, Money amount) {
        try {
            banksClient.adjustBalance(accountId.value(), amount.amount(), amount.currency().getCurrencyCode());
        } catch (FeignException e) {
            throw new InfrastructureException("Banks service unavailable: " + e.getMessage(), e);
        }
    }
}
