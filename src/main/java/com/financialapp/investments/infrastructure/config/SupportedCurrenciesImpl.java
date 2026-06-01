package com.financialapp.investments.infrastructure.config;

import com.financialapp.investments.domain.gateway.SupportedCurrencies;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SupportedCurrenciesImpl implements SupportedCurrencies {

    private final CurrenciesProperties props;
    private Set<Currency> cached;

    @PostConstruct
    void init() {
        cached = props.supported().stream()
                .map(Currency::getInstance)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean isSupported(Currency currency) {
        return cached.contains(currency);
    }

    @Override
    public Set<Currency> all() {
        return cached;
    }
}
