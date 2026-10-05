package com.financialapp.investments.infrastructure.config;

import com.financialapp.investments.domain.service.BrokerFeeNetting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrokerFeeConfig {

    @Bean
    public BrokerFeeNetting brokerFeeNetting() {
        return new BrokerFeeNetting();
    }
}
