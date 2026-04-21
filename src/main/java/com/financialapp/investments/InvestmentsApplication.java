package com.financialapp.investments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@ConfigurationPropertiesScan
@EnableFeignClients
public class InvestmentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestmentsApplication.class, args);
    }
}
