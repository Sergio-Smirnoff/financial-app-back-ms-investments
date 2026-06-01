package com.financialapp.investments.infrastructure.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(
            @Value("${internal.auth.token}") String internalToken) {
        return requestTemplate -> requestTemplate.header("X-Internal-Token", internalToken);
    }
}
