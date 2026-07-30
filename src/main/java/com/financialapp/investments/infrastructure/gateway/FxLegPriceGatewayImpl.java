package com.financialapp.investments.infrastructure.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.financialapp.investments.domain.gateway.FxLegPriceGateway;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.infrastructure.config.FxPairsProperties;
import com.financialapp.investments.infrastructure.gateway.dto.IolHistoricalPricePoint;
import com.financialapp.investments.infrastructure.gateway.dto.IolPriceDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class FxLegPriceGatewayImpl implements FxLegPriceGateway {

    private final IolApiClient iolApiClient;
    private final FxPairsProperties fxPairsProperties;
    private final RestTemplate restTemplate;

    @org.springframework.beans.factory.annotation.Autowired
    public FxLegPriceGatewayImpl(IolApiClient iolApiClient, FxPairsProperties fxPairsProperties) {
        this(iolApiClient, fxPairsProperties, new RestTemplate());
    }

    public FxLegPriceGatewayImpl(IolApiClient iolApiClient, FxPairsProperties fxPairsProperties, RestTemplate restTemplate) {
        this.iolApiClient = iolApiClient;
        this.fxPairsProperties = fxPairsProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<BigDecimal> priceOn(String ticker, LocalDate date) {
        if (date == null || ticker == null || ticker.isBlank()) {
            return Optional.empty();
        }

        if (date.equals(LocalDate.now())) {
            try {
                Optional<IolPriceDetail> detail = iolApiClient.getPrice(ticker, AssetType.BOND);
                return detail.map(IolPriceDetail::lastPrice);
            } catch (Exception e) {
                log.error("Failed to fetch live price for ticker={}: {}", ticker, e.getMessage());
                return Optional.empty();
            }
        } else {
            try {
                List<IolHistoricalPricePoint> points = iolApiClient.getHistoricalSeries(ticker, AssetType.BOND, date, date);
                if (points == null || points.isEmpty()) {
                    return Optional.empty();
                }
                IolHistoricalPricePoint lastPoint = points.get(points.size() - 1);
                return Optional.ofNullable(lastPoint.detail()).map(IolPriceDetail::lastPrice);
            } catch (Exception e) {
                log.error("Failed to fetch historical price for ticker={} date={}: {}", ticker, date, e.getMessage());
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<BigDecimal> oficialRateOn(LocalDate date) {
        if (date == null) {
            return Optional.empty();
        }
        String bcraUrl = (fxPairsProperties != null && fxPairsProperties.oficial() != null)
                ? fxPairsProperties.oficial().bcraUrl()
                : "https://api.bcra.gob.ar/estadisticas/v3.0/Moneda";

        try {
            JsonNode response = restTemplate.getForObject(bcraUrl, JsonNode.class);
            if (response == null) {
                return Optional.empty();
            }
            if (response.has("results") && response.get("results").isArray()) {
                for (JsonNode item : response.get("results")) {
                    if (item.has("valor")) {
                        return Optional.of(new BigDecimal(item.get("valor").asText()));
                    }
                }
            } else if (response.has("valor")) {
                return Optional.of(new BigDecimal(response.get("valor").asText()));
            }
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("Failed to fetch BCRA oficial rate for date={}: {}", date, ex.getMessage());
            return Optional.empty();
        }
    }
}
