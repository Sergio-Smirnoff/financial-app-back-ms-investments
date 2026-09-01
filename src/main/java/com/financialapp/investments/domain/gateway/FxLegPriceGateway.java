package com.financialapp.investments.domain.gateway;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface FxLegPriceGateway {
    Optional<BigDecimal> priceOn(String ticker, LocalDate date);
    Optional<BigDecimal> oficialRateOn(LocalDate date);
}
