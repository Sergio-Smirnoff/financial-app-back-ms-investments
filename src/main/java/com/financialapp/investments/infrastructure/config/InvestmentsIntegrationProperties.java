package com.financialapp.investments.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "investments.integration")
@Getter
@Setter
public class InvestmentsIntegrationProperties {

    private Long financesCategoryId;

    private Map<String, String> brokerCbu;

    public String brokerCbuFor(String currencyCode) {
        String cbu = brokerCbu != null ? brokerCbu.get(currencyCode.toUpperCase()) : null;
        if (cbu == null) {
            throw new IllegalStateException("No broker settlement CBU configured for currency " + currencyCode);
        }
        return cbu;
    }
}
