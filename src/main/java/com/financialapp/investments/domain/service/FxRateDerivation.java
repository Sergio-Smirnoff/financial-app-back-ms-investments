package com.financialapp.investments.domain.service;

import com.financialapp.investments.domain.gateway.FxLegPriceGateway;
import com.financialapp.investments.domain.model.fx.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FxRateDerivation {

    private final FxLegPriceGateway legPriceGateway;
    private final FxBondPairs bondPairs;

    public FxRateDerivation(FxLegPriceGateway legPriceGateway, FxBondPairs bondPairs) {
        this.legPriceGateway = legPriceGateway;
        this.bondPairs = bondPairs;
    }

    public FxSnapshotRates deriveRates(LocalDate date) {
        BigDecimal mepRate = computeRatio(bondPairs.mepArsTicker(), bondPairs.mepUsdTicker(), date);
        BigDecimal cclRate = computeRatio(bondPairs.cclArsTicker(), bondPairs.cclUsdTicker(), date);
        BigDecimal oficialRate = legPriceGateway.oficialRateOn(date).orElse(null);

        return new FxSnapshotRates(mepRate, cclRate, oficialRate, date);
    }

    public List<FxRate> deriveAsFxRates(LocalDate date) {
        FxSnapshotRates snapshot = deriveRates(date);
        List<FxRate> rates = new ArrayList<>();

        if (snapshot.mepRate() != null) {
            rates.add(new FxRate(null, date, FxView.MEP, snapshot.mepRate(), snapshot.mepRate(), FxRateSource.IOL_SYNTHETIC, null));
        }
        if (snapshot.cclRate() != null) {
            rates.add(new FxRate(null, date, FxView.CCL, snapshot.cclRate(), snapshot.cclRate(), FxRateSource.IOL_SYNTHETIC, null));
        }
        if (snapshot.oficialRate() != null) {
            rates.add(new FxRate(null, date, FxView.OFICIAL, snapshot.oficialRate(), snapshot.oficialRate(), FxRateSource.IOL_SYNTHETIC, null));
        }

        return rates;
    }

    private BigDecimal computeRatio(String arsTicker, String usdTicker, LocalDate date) {
        Optional<BigDecimal> arsPriceOpt = legPriceGateway.priceOn(arsTicker, date);
        Optional<BigDecimal> usdPriceOpt = legPriceGateway.priceOn(usdTicker, date);

        if (arsPriceOpt.isEmpty() || usdPriceOpt.isEmpty()) {
            return null;
        }

        BigDecimal arsPrice = arsPriceOpt.get();
        BigDecimal usdPrice = usdPriceOpt.get();

        if (usdPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return arsPrice.divide(usdPrice, 4, RoundingMode.HALF_EVEN);
    }
}
