package com.financialapp.investments.infrastructure.gateway;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IolCurrencyResolverTest {

    @Test
    void resolvesDolarMonedaToUsd() {
        assertThat(IolCurrencyResolver.resolve("dolar_Estadounidense")).isEqualTo("USD");
        assertThat(IolCurrencyResolver.resolve("Dolar")).isEqualTo("USD");
    }

    @Test
    void resolvesPesoMonedaToArs() {
        assertThat(IolCurrencyResolver.resolve("peso_Argentino")).isEqualTo("ARS");
    }

    @Test
    void fallsBackToArsWhenMonedaMissingOrUnknown() {
        assertThat(IolCurrencyResolver.resolve(null)).isEqualTo("ARS");
        assertThat(IolCurrencyResolver.resolve("")).isEqualTo("ARS");
        assertThat(IolCurrencyResolver.resolve("something_else")).isEqualTo("ARS");
    }
}
