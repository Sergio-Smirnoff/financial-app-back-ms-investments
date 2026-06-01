package com.financialapp.investments.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshotId;
import com.financialapp.investments.infrastructure.persistence.entity.PortfolioSnapshotJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PortfolioSnapshotPersistenceMapper {

    private final ObjectMapper objectMapper;

    public PortfolioSnapshot toDomain(PortfolioSnapshotJpaEntity e) {
        return new PortfolioSnapshot(
                new PortfolioSnapshotId(e.getId()),
                new UserId(e.getUserId()),
                e.getSnapshotDate(),
                deserializeTotals(e.getTotals()),
                e.getCreatedAt()
        );
    }

    public PortfolioSnapshotJpaEntity toEntity(PortfolioSnapshot s) {
        return PortfolioSnapshotJpaEntity.builder()
                .id(s.id() != null ? s.id().value() : null)
                .userId(s.userId().value())
                .snapshotDate(s.snapshotDate())
                .totals(serializeTotals(s.totals()))
                .createdAt(s.createdAt())
                .build();
    }

    String serializeTotals(List<Money> totals) {
        Map<String, BigDecimal> map = totals.stream()
                .collect(Collectors.toMap(
                        m -> m.currency().getCurrencyCode(),
                        Money::amount,
                        BigDecimal::add,
                        LinkedHashMap::new));
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize portfolio snapshot totals", ex);
        }
    }

    List<Money> deserializeTotals(String json) {
        Map<String, BigDecimal> raw;
        try {
            raw = objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize portfolio snapshot totals", ex);
        }
        return raw.entrySet().stream()
                .map(e -> new Money(e.getValue(), Currency.getInstance(e.getKey())))
                .sorted(Comparator.comparing(m -> m.currency().getCurrencyCode()))
                .toList();
    }
}
