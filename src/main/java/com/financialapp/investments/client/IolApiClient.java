package com.financialapp.investments.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.financialapp.investments.config.IolProperties;
import com.financialapp.investments.model.dto.internal.HistoricalPricePoint;
import com.financialapp.investments.model.dto.internal.PriceDetail;
import com.financialapp.investments.model.enums.AssetType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class IolApiClient {

    private final IolProperties properties;
    private final RestTemplate restTemplate;

    private volatile String accessToken;
    private volatile Instant tokenExpiry;

    public IolApiClient(IolProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    public Optional<PriceDetail> getPrice(String ticker, AssetType assetType) {
        try {
            ensureAuthenticated();
            String market = resolveMarket(assetType);
            String url = properties.getBaseUrl() + "/api/v2/" + market + "/Titulos/" + ticker + "/CotizacionDetalle";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode body = response.getBody();
                JsonNode priceNode = body.get("ultimoPrecio");
                if (priceNode != null && !priceNode.isNull()) {
                    return Optional.of(new PriceDetail(
                            new BigDecimal(priceNode.asText()),
                            parseBigDecimal(body, "apertura"),
                            parseBigDecimal(body, "maximo"),
                            parseBigDecimal(body, "minimo"),
                            parseBigDecimal(body, "volumen"),
                            parseBigDecimal(body, "variacion")
                    ));
                }
            }
            log.warn("No price returned for ticker={} market={}", ticker, market);
            return Optional.empty();
        } catch (RestClientException ex) {
            log.error("Failed to fetch price for ticker={}: {}", ticker, ex.getMessage());
            return Optional.empty();
        }
    }

    public List<HistoricalPricePoint> getHistoricalSeries(String ticker, AssetType assetType, LocalDate from, LocalDate to) {
        try {
            ensureAuthenticated();
            String market = resolveMarket(assetType);
            // Canonical IOL historical endpoint: path params, not query string
            String url = properties.getBaseUrl() + "/api/v2/" + market + "/Titulos/" + ticker
                    + "/Cotizacion/seriehistorica/" + from + "/" + to + "/sinAjustar";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("No series data for ticker={}", ticker);
                return List.of();
            }

            JsonNode body = response.getBody();
            log.info("IOL seriehistorica response for ticker={}: {} items", ticker,
                    body.isArray() ? body.size() : "non-array");
            List<HistoricalPricePoint> points = new ArrayList<>();

            for (JsonNode item : body) {
                // seriehistorica uses same structure as CotizacionDetalle: ultimoPrecio + fechaHora
                JsonNode fechaNode = item.get("fechaHora");
                JsonNode priceNode = item.get("ultimoPrecio");
                if (fechaNode == null || fechaNode.isNull() || priceNode == null || priceNode.isNull()) {
                    log.debug("Skipping item missing fechaHora/ultimoPrecio: {}", item);
                    continue;
                }

                // Trim to 19 chars to strip timezone offset (e.g. "2026-04-14T17:00:00-03:00")
                LocalDateTime pricedAt = LocalDateTime.parse(fechaNode.asText().substring(0, 19),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

                PriceDetail detail = new PriceDetail(
                        new BigDecimal(priceNode.asText()),
                        parseBigDecimal(item, "apertura"),
                        parseBigDecimal(item, "maximo"),
                        parseBigDecimal(item, "minimo"),
                        parseBigDecimal(item, "volumenNominal"),
                        parseBigDecimal(item, "variacion")
                );
                points.add(new HistoricalPricePoint(pricedAt, detail));
            }
            return points;
        } catch (RestClientException ex) {
            log.error("Failed to fetch series for ticker={}: {}", ticker, ex.getMessage());
            return List.of();
        }
    }

    private BigDecimal parseBigDecimal(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? new BigDecimal(n.asText()) : null;
    }

    private synchronized void ensureAuthenticated() {
        if (accessToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return;
        }
        authenticate();
    }

    private void authenticate() {
        String url = properties.getBaseUrl() + "/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("username", properties.getUsername());
        body.add("password", properties.getPassword());
        body.add("grant_type", "password");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode responseBody = response.getBody();
                accessToken = responseBody.get("access_token").asText();
                int expiresIn = responseBody.get("expires_in").asInt();
                tokenExpiry = Instant.now().plusSeconds(expiresIn - 60); // refresh 1 min early
                log.info("IOL authentication successful, token expires in {} seconds", expiresIn);
            } else {
                throw new RuntimeException("IOL authentication failed with status: " + response.getStatusCode());
            }
        } catch (RestClientException ex) {
            log.error("IOL authentication failed: {}", ex.getMessage());
            throw new RuntimeException("IOL authentication failed", ex);
        }
    }

    private String resolveMarket(AssetType assetType) {
        return switch (assetType) {
            case FCI -> "fci";
            case STOCK, BOND, CEDEAR -> "bCBA";
        };
    }
}
