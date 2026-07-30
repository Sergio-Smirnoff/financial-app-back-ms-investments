package com.financialapp.investments.infrastructure.config;

import com.financialapp.investments.domain.model.fx.FxBondPairs;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fx")
public record FxPairsProperties(
        Pairs pairs,
        Oficial oficial
) {
    public record Pairs(
            String mepArsTicker,
            String mepUsdTicker,
            String cclArsTicker,
            String cclUsdTicker
    ) {
        public Pairs {
            if (mepArsTicker == null || mepArsTicker.isBlank()) mepArsTicker = "AL30";
            if (mepUsdTicker == null || mepUsdTicker.isBlank()) mepUsdTicker = "AL30D";
            if (cclArsTicker == null || cclArsTicker.isBlank()) cclArsTicker = "AL30";
            if (cclUsdTicker == null || cclUsdTicker.isBlank()) cclUsdTicker = "AL30C";
        }
    }

    public record Oficial(
            String bcraUrl
    ) {
        public Oficial {
            if (bcraUrl == null || bcraUrl.isBlank()) {
                bcraUrl = "https://api.bcra.gob.ar/estadisticas/v3.0/Moneda";
            }
        }
    }

    public FxBondPairs toDomain() {
        Pairs p = pairs != null ? pairs : new Pairs(null, null, null, null);
        return new FxBondPairs(p.mepArsTicker(), p.mepUsdTicker(), p.cclArsTicker(), p.cclUsdTicker());
    }
}
