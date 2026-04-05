package com.financialapp.investments.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "iol")
public class IolProperties {
    private String baseUrl;
    private String username;
    private String password;
    private String priceRefreshCron;
}
