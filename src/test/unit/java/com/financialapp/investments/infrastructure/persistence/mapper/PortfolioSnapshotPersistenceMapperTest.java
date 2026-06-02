package com.financialapp.investments.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshotId;
import com.financialapp.investments.infrastructure.persistence.entity.PortfolioSnapshotJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PortfolioSnapshotPersistenceMapperTest {

    private PortfolioSnapshotPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PortfolioSnapshotPersistenceMapper(new ObjectMapper());
    }

    @Test
    void roundTrip_preservesAllCurrencies() {
        List<Money> totals = List.of(
                new Money(new BigDecimal("12345.67"), Currency.getInstance("ARS")),
                new Money(new BigDecimal("678.90"), Currency.getInstance("USD")),
                new Money(new BigDecimal("90.10"), Currency.getInstance("BRL")));
        PortfolioSnapshot original = new PortfolioSnapshot(
                new PortfolioSnapshotId(1L),
                new UserId(42L),
                LocalDate.of(2026, 5, 31),
                totals,
                LocalDateTime.of(2026, 5, 31, 0, 0));

        PortfolioSnapshotJpaEntity entity = mapper.toEntity(original);
        assertThat(entity.getTotals()).contains("\"ARS\"", "\"USD\"", "\"BRL\"");

        PortfolioSnapshot roundTripped = mapper.toDomain(entity);
        assertThat(roundTripped.totals())
                .extracting(m -> m.currency().getCurrencyCode())
                .containsExactly("ARS", "BRL", "USD"); // sorted alpha
    }

    @Test
    void deserialize_emptyJsonObject_returnsEmptyList() {
        assertThat(mapper.deserializeTotals("{}")).isEmpty();
    }

    @Test
    void deserialize_malformedJson_throws() {
        assertThatThrownBy(() -> mapper.deserializeTotals("{not json"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void serialize_singleCurrency_producesExpectedShape() {
        List<Money> totals = List.of(
                new Money(new BigDecimal("100.50"), Currency.getInstance("USD")));
        String json = mapper.serializeTotals(totals);
        assertThat(json).contains("USD").contains("100.5");
    }
}
