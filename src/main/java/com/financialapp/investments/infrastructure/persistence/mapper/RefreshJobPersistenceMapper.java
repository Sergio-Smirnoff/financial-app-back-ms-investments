package com.financialapp.investments.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.investments.domain.model.refresh.RefreshJob;
import com.financialapp.investments.domain.model.refresh.RefreshJobId;
import com.financialapp.investments.infrastructure.persistence.entity.RefreshJobJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RefreshJobPersistenceMapper {

    private final ObjectMapper objectMapper;

    public RefreshJob toDomain(RefreshJobJpaEntity e) {
        return new RefreshJob(
                new RefreshJobId(e.getId()),
                e.getStatus(),
                deserializeTickers(e.getAllTickers()),
                e.getLastSuccessIndex(),
                e.getFailureReason(),
                e.getStartedAt(),
                e.getCompletedAt(),
                e.getUpdatedAt()
        );
    }

    public RefreshJobJpaEntity toEntity(RefreshJob j) {
        return RefreshJobJpaEntity.builder()
                .id(j.id() != null ? j.id().value() : null)
                .status(j.status())
                .allTickers(serializeTickers(j.allTickers()))
                .lastSuccessIndex(j.lastSuccessIndex())
                .failureReason(j.failureReason())
                .startedAt(j.startedAt())
                .completedAt(j.completedAt())
                .updatedAt(j.updatedAt())
                .build();
    }

    private String serializeTickers(List<String> tickers) {
        try {
            return objectMapper.writeValueAsString(tickers);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize tickers", e);
        }
    }

    private List<String> deserializeTickers(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize tickers", e);
        }
    }
}
