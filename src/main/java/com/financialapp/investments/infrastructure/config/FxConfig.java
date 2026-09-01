package com.financialapp.investments.infrastructure.config;

import com.financialapp.investments.domain.gateway.FxLegPriceGateway;
import com.financialapp.investments.domain.service.FxRateDerivation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FxConfig {

    @Bean
    public FxRateDerivation fxRateDerivation(FxLegPriceGateway legPriceGateway, FxPairsProperties pairsProperties) {
        return new FxRateDerivation(legPriceGateway, pairsProperties.toDomain());
    }
}
