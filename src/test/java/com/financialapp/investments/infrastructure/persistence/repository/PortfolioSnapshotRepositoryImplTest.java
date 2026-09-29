package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshotId;
import com.financialapp.investments.infrastructure.persistence.entity.PortfolioSnapshotJpaEntity;
import com.financialapp.investments.infrastructure.persistence.jpa.PortfolioSnapshotJpaRepository;
import com.financialapp.investments.infrastructure.persistence.mapper.PortfolioSnapshotPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioSnapshotRepositoryImplTest {

    private static final LocalDate DAY = LocalDate.of(2026, 9, 22);
    private static final PortfolioSnapshot SNAPSHOT = new PortfolioSnapshot(
            new PortfolioSnapshotId(null), new UserId(7L), DAY, List.of(), LocalDateTime.of(2026, 9, 22, 8, 30));

    @Mock private PortfolioSnapshotJpaRepository jpaRepository;
    @Mock private PortfolioSnapshotPersistenceMapper mapper;
    @InjectMocks private PortfolioSnapshotRepositoryImpl repository;

    @Test
    void existsForDate_asksForThatUsersRowOnThatDate() {
        when(jpaRepository.existsByUserIdAndSnapshotDate(7L, DAY)).thenReturn(true);

        assertThat(repository.existsForDate(new UserId(7L), DAY)).isTrue();
    }

    @Test
    void saveIfAbsent_newRow_isFlushedAndReportsTrue() {
        PortfolioSnapshotJpaEntity entity = new PortfolioSnapshotJpaEntity();
        when(mapper.toEntity(SNAPSHOT)).thenReturn(entity);

        assertThat(repository.saveIfAbsent(SNAPSHOT)).isTrue();
        verify(jpaRepository).saveAndFlush(entity);
    }

    @Test
    void saveIfAbsent_uniqueViolation_meansAnotherCallerWon() {
        when(mapper.toEntity(SNAPSHOT)).thenReturn(new PortfolioSnapshotJpaEntity());
        when(jpaRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uq"));

        assertThat(repository.saveIfAbsent(SNAPSHOT)).isFalse();
    }
}
