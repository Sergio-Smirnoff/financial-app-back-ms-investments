package com.financialapp.investments.infrastructure.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.financialapp.investments.infrastructure.gateway.dto.IolHistoricalPricePoint;
import com.financialapp.investments.infrastructure.gateway.dto.IolMarketQuote;
import com.financialapp.investments.infrastructure.gateway.dto.IolPriceDetail;
import com.financialapp.investments.infrastructure.config.IolProperties;
import com.financialapp.investments.domain.model.price.AssetType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
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

    @Retry(name = "iolApi")
    @CircuitBreaker(name = "iolApi")
    public Optional<IolPriceDetail> getPrice(String ticker, AssetType assetType) {
        try {
            return getPriceInternal(ticker, assetType);
        } catch (HttpClientErrorException.Unauthorized ex) {
            log.info("IOL token expired or invalid (401), refreshing...");
            authenticate();
            return getPriceInternal(ticker, assetType);
        } catch (RestClientException ex) {
            log.error("Failed to fetch price for ticker={}: {}", ticker, ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<IolPriceDetail> getPriceInternal(String ticker, AssetType assetType) {
        ensureAuthenticated();
        String market = resolveMarket(assetType);
        String url = properties.baseUrl() + "/api/v2/" + market + "/Titulos/" + ticker + "/CotizacionDetalle";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode body = response.getBody();
            JsonNode priceNode = body.get("ultimoPrecio");
            if (priceNode != null && !priceNode.isNull()) {
                return Optional.of(new IolPriceDetail(
                        new BigDecimal(priceNode.asText()),
                        parseBigDecimal(body, "apertura"),
                        parseBigDecimal(body, "maximo"),
                        parseBigDecimal(body, "minimo"),
                        parseBigDecimal(body, "volumen"),
                        parseBigDecimal(body, "variacion"),
                        IolCurrencyResolver.resolve(parseText(body, "moneda"))
                ));
            }
        }
        log.warn("No price returned for ticker={} market={}", ticker, market);
        return Optional.empty();
    }

    @Retry(name = "iolApi")
    @CircuitBreaker(name = "iolApi")
    public List<IolHistoricalPricePoint> getHistoricalSeries(String ticker, AssetType assetType, LocalDate from, LocalDate to) {
        try {
            return getHistoricalSeriesInternal(ticker, assetType, from, to);
        } catch (HttpClientErrorException.Unauthorized ex) {
            log.info("IOL token expired or invalid (401), refreshing...");
            authenticate();
            return getHistoricalSeriesInternal(ticker, assetType, from, to);
        } catch (RestClientException ex) {
            log.error("Failed to fetch series for ticker={}: {}", ticker, ex.getMessage());
            return List.of();
        }
    }

    private List<IolHistoricalPricePoint> getHistoricalSeriesInternal(String ticker, AssetType assetType, LocalDate from, LocalDate to) {
        ensureAuthenticated();
        String market = resolveMarket(assetType);
        // Canonical IOL historical endpoint: path params, not query string
        String url = properties.baseUrl() + "/api/v2/" + market + "/Titulos/" + ticker
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
        List<IolHistoricalPricePoint> points = new ArrayList<>();

        for (JsonNode item : body) {
            // seriehistorica uses same structure as CotizacionDetalle: ultimoPrecio + fechaHora
            JsonNode dateNode = item.get("fechaHora");
            JsonNode priceNode = item.get("ultimoPrecio");
            if (dateNode == null || dateNode.isNull() || priceNode == null || priceNode.isNull()) {
                log.debug("Skipping item missing fechaHora/ultimoPrecio: {}", item);
                continue;
            }

            // Trim to 19 chars to strip timezone offset (e.g. "2026-04-14T17:00:00-03:00")
            LocalDateTime pricedAt = LocalDateTime.parse(dateNode.asText().substring(0, 19),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            IolPriceDetail detail = new IolPriceDetail(
                    new BigDecimal(priceNode.asText()),
                    parseBigDecimal(item, "apertura"),
                    parseBigDecimal(item, "maximo"),
                    parseBigDecimal(item, "minimo"),
                    parseBigDecimal(item, "volumenNominal"),
                    parseBigDecimal(item, "variacion"),
                    IolCurrencyResolver.resolve(parseText(item, "moneda"))
            );
            points.add(new IolHistoricalPricePoint(pricedAt, detail));
        }
        return points;
    }

    @Retry(name = "iolApi")
    @CircuitBreaker(name = "iolApi")
    public List<IolMarketQuote> getPanelQuotes(String market) {
        try {
            return getPanelQuotesInternal(market);
        } catch (HttpClientErrorException.Unauthorized ex) {
            log.info("IOL token expired or invalid (401), refreshing...");
            authenticate();
            return getPanelQuotesInternal(market);
        } catch (Exception ex) {
            log.error("Critical error fetching panel quotes for {}: {}", market, ex.getMessage());
            throw new RuntimeException("IOL panel quotes fetch failed for " + market, ex);
        }
    }

    private List<IolMarketQuote> getPanelQuotesInternal(String market) {
        ensureAuthenticated();
        String url = properties.baseUrl() + "/api/v2/Cotizaciones/Acciones/" + market + "/Argentina";
        log.info("Fetching panel quotes from: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("IOL panel quotes returned non-success status " + response.getStatusCode() + " for market " + market);
        }

        JsonNode titulos = response.getBody().get("titulos");
        List<IolMarketQuote> quotes = new ArrayList<>();

        if (titulos != null && titulos.isArray()) {
            log.info("Found {} titles in panel {}", titulos.size(), market);
            for (JsonNode node : titulos) {
                quotes.add(new IolMarketQuote(
                        node.get("simbolo").asText(),
                        parseBigDecimal(node, "ultimoPrecio"),
                        parseBigDecimal(node, "variacion"),
                        IolCurrencyResolver.resolve(parseText(node, "moneda"))
                ));
            }
        } else {
            log.warn("IOL response body missing 'titulos' array: {}", response.getBody());
        }
        return quotes;
    }

    private BigDecimal parseBigDecimal(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? new BigDecimal(n.asText()) : null;
    }

    private String parseText(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asText() : null;
    }

    private synchronized void ensureAuthenticated() {
        if (accessToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return;
        }
        authenticate();
    }

    private void authenticate() {
        String url = properties.baseUrl() + "/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("username", properties.username());
        body.add("password", properties.password());
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
