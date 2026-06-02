package com.financialapp.investments;

import com.financialapp.investments.domain.gateway.SupportedCurrencies;
import com.financialapp.investments.infrastructure.config.CurrenciesProperties;
import com.financialapp.investments.infrastructure.config.FeignConfig;
import com.financialapp.investments.infrastructure.config.InternalAuthFilter;
import com.financialapp.investments.infrastructure.config.IolProperties;
import feign.RequestInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ConfigurationBindingTest {

    @Autowired IolProperties iolProperties;
    @Autowired CurrenciesProperties currenciesProperties;
    @Autowired ApplicationContext ctx;

    @MockBean
    KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void iolPropertiesBound_allFieldsPresent() {
        assertThat(iolProperties.baseUrl()).isNotBlank();
        assertThat(iolProperties.username()).isNotBlank();
        assertThat(iolProperties.password()).isNotBlank();
        assertThat(iolProperties.priceRefreshCron()).isNotBlank();
    }

    @Test
    void currenciesPropertiesBound_supportedListPopulated() {
        assertThat(currenciesProperties.supported())
                .isNotEmpty()
                .allMatch(c -> c.matches("[A-Z]{3}"));
    }

    @Test
    void supportedCurrenciesPort_initialisedWithJdkCurrencies() {
        SupportedCurrencies port = ctx.getBean(SupportedCurrencies.class);
        assertThat(port.all()).isNotEmpty();
        assertThat(port.isSupported(Currency.getInstance("ARS"))).isTrue();
    }

    @Test
    void internalAuthFilterBeanCreated_passesPostConstructValidation() {
        assertThat(ctx.getBean(InternalAuthFilter.class)).isNotNull();
    }

    @Test
    void feignRequestInterceptorBean_createdFromInternalAuthToken() {
        assertThat(ctx.getBean(RequestInterceptor.class)).isNotNull();
        assertThat(ctx.getBean(FeignConfig.class)).isNotNull();
    }
}
