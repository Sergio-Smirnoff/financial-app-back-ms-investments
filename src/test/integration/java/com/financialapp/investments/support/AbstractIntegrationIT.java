package com.financialapp.investments.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.infrastructure.persistence.entity.AssetPriceHistoryJpaEntity;
import com.financialapp.investments.infrastructure.persistence.entity.AssetPriceJpaEntity;
import com.financialapp.investments.infrastructure.persistence.entity.HoldingJpaEntity;
import com.financialapp.investments.infrastructure.persistence.entity.MarketPanelQuoteJpaEntity;
import com.financialapp.investments.infrastructure.persistence.entity.PortfolioSnapshotJpaEntity;
import com.financialapp.investments.infrastructure.persistence.jpa.AssetPriceHistoryJpaRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.AssetPriceJpaRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.HoldingJpaRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.MarketPanelQuoteJpaRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.PortfolioSnapshotJpaRepository;
import com.financialapp.investments.infrastructure.persistence.jpa.RefreshJobJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Base for controller / persistence integration tests.
 *
 * <p>Boots the full Spring context against H2 (profile {@code test}, see
 * {@code src/test/resources/application-test.yml}). The {@code test} profile excludes
 * Kafka auto-config, so {@link KafkaTemplate} (required by the transactional Kafka
 * listener) is supplied as a mock — the same approach as {@code ConfigurationBindingTest}.
 * Tests that need a real broker extend {@link AbstractKafkaIT} instead.
 *
 * <p>The {@code InternalAuthFilter} guards every endpoint: requests must carry
 * {@code X-Internal-Token: test-token}. Use {@link #authGet(String)} to build such requests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationIT {

    protected static final String INTERNAL_TOKEN = "test-token";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected HoldingJpaRepository holdingRepository;
    @Autowired protected AssetPriceJpaRepository assetPriceRepository;
    @Autowired protected AssetPriceHistoryJpaRepository assetPriceHistoryRepository;
    @Autowired protected MarketPanelQuoteJpaRepository marketQuoteRepository;
    @Autowired protected PortfolioSnapshotJpaRepository snapshotRepository;
    @Autowired protected RefreshJobJpaRepository refreshJobRepository;

    // Kafka auto-config is excluded under the test profile; satisfy the listener's dependency.
    @MockBean protected KafkaTemplate<String, Object> kafkaTemplate;

    @BeforeEach
    void cleanDatabase() {
        refreshJobRepository.deleteAll();
        assetPriceHistoryRepository.deleteAll();
        assetPriceRepository.deleteAll();
        marketQuoteRepository.deleteAll();
        snapshotRepository.deleteAll();
        holdingRepository.deleteAll();
    }

    /** GET request pre-authenticated with the internal token. */
    protected MockHttpServletRequestBuilder authGet(String url) {
        return get(url).header("X-Internal-Token", INTERNAL_TOKEN);
    }

    // --- seeding helpers ------------------------------------------------------

    protected HoldingJpaEntity seedHolding(Long userId, Long bankAccountId, String ticker,
                                           AssetType assetType, String quantity, String avgPrice,
                                           String currency, String gainPct, String lossPct) {
        LocalDateTime now = LocalDateTime.now();
        return holdingRepository.save(HoldingJpaEntity.builder()
                .userId(userId)
                .bankAccountId(bankAccountId)
                .bankId(1L)
                .ticker(ticker)
                .name(ticker + " Inc")
                .assetType(assetType)
                .quantity(new BigDecimal(quantity))
                .avgPurchasePrice(new BigDecimal(avgPrice))
                .currency(currency)
                .notifyGainThresholdPct(gainPct != null ? new BigDecimal(gainPct) : null)
                .notifyLossThresholdPct(lossPct != null ? new BigDecimal(lossPct) : null)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    protected AssetPriceJpaEntity seedAssetPrice(String ticker, AssetType assetType,
                                                 String lastPrice, String currency) {
        LocalDateTime now = LocalDateTime.now();
        return assetPriceRepository.save(AssetPriceJpaEntity.builder()
                .ticker(ticker)
                .assetType(assetType)
                .lastPrice(new BigDecimal(lastPrice))
                .currency(currency)
                .openPrice(new BigDecimal(lastPrice))
                .highPrice(new BigDecimal(lastPrice))
                .lowPrice(new BigDecimal(lastPrice))
                .volume(new BigDecimal("1000"))
                .dailyVariation(new BigDecimal("1.0"))
                .pricedAt(now)
                .updatedAt(now)
                .build());
    }

    protected AssetPriceHistoryJpaEntity seedHistory(String ticker, AssetType assetType,
                                                     String lastPrice, String currency,
                                                     LocalDateTime pricedAt) {
        return assetPriceHistoryRepository.save(AssetPriceHistoryJpaEntity.builder()
                .ticker(ticker)
                .assetType(assetType)
                .lastPrice(new BigDecimal(lastPrice))
                .openPrice(new BigDecimal(lastPrice))
                .highPrice(new BigDecimal(lastPrice))
                .lowPrice(new BigDecimal(lastPrice))
                .volume(new BigDecimal("1000"))
                .dailyVariation(new BigDecimal("0.5"))
                .currency(currency)
                .pricedAt(pricedAt)
                .build());
    }

    protected MarketPanelQuoteJpaEntity seedMarketQuote(String ticker, String lastPrice,
                                                        String currency, String variation) {
        return marketQuoteRepository.save(MarketPanelQuoteJpaEntity.builder()
                .ticker(ticker)
                .lastPrice(new BigDecimal(lastPrice))
                .currency(currency)
                .variation(new BigDecimal(variation))
                .volume(new BigDecimal("5000"))
                .updatedAt(LocalDateTime.now())
                .build());
    }

    protected PortfolioSnapshotJpaEntity seedSnapshot(Long userId, LocalDate date, String totalsJson) {
        return snapshotRepository.save(PortfolioSnapshotJpaEntity.builder()
                .userId(userId)
                .snapshotDate(date)
                .totals(totalsJson)
                .createdAt(LocalDateTime.now())
                .build());
    }
}
