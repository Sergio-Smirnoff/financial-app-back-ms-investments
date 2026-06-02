package com.financialapp.investments.infrastructure.gateway.impl;

import com.financialapp.investments.domain.common.model.Cbu;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.exception.FinancesServiceException;
import com.financialapp.investments.domain.gateway.FinancesGateway;
import com.financialapp.investments.infrastructure.config.InvestmentsIntegrationProperties;
import com.financialapp.investments.infrastructure.gateway.FinancesClient;
import com.financialapp.investments.infrastructure.gateway.dto.RecordTransactionRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class FinancesGatewayImpl implements FinancesGateway {

    private final FinancesClient financesClient;
    private final InvestmentsIntegrationProperties properties;

    @Override
    public void recordPurchase(UserId userId, Cbu fundingCbu, Money totalCost) {
        String currency = totalCost.currency().getCurrencyCode();
        Cbu broker = new Cbu(properties.brokerCbuFor(currency));
        send(userId, fundingCbu, broker, totalCost, "Investment purchase");
    }

    @Override
    public void recordSaleProceeds(UserId userId, Cbu destinationCbu, Money proceeds) {
        String currency = proceeds.currency().getCurrencyCode();
        Cbu broker = new Cbu(properties.brokerCbuFor(currency));
        send(userId, broker, destinationCbu, proceeds, "Investment sale proceeds");
    }

    private void send(UserId userId, Cbu fromCbu, Cbu toCbu, Money amount, String description) {
        RecordTransactionRequest request = new RecordTransactionRequest(
                fromCbu.value(),
                toCbu.value(),
                amount.amount().toPlainString(),
                amount.currency().getCurrencyCode(),
                properties.getFinancesCategoryId(),
                description,
                LocalDate.now());
        try {
            financesClient.recordTransaction(userId.value(), request);
        } catch (FeignException e) {
            throw new FinancesServiceException("Failed to record investment transaction in finances", e);
        }
    }
}
