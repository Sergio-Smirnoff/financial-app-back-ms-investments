package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxRateId;
import com.financialapp.investments.domain.model.fx.FxRateSource;
import com.financialapp.investments.domain.model.fx.FxView;
import com.financialapp.investments.infrastructure.persistence.entity.FxRateJpaEntity;
import com.financialapp.investments.infrastructure.persistence.jpa.FxRateJpaRepository;
import com.financialapp.investments.infrastructure.persistence.mapper.FxRatePersistenceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FxRateRepositoryImplTest {

    @Mock
    private FxRateJpaRepository jpaRepository;

    private FxRatePersistenceMapper mapper;
    private FxRateRepositoryImpl repository;

    private static final LocalDate DATE = LocalDate.of(2026, 7, 29);
    private static final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        mapper = new FxRatePersistenceMapper();
        repository = new FxRateRepositoryImpl(jpaRepository, mapper);
    }

    @Test
    void save_newRecord_insertsEntity() {
        FxRate rate = new FxRate(null, DATE, FxView.MEP, new BigDecimal("1300"), new BigDecimal("1310"), FxRateSource.IOL_SYNTHETIC, NOW);
        given(jpaRepository.findByRateDateAndFxView(DATE, FxView.MEP)).willReturn(Optional.empty());
        given(jpaRepository.save(any())).willAnswer(invocation -> {
            FxRateJpaEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        FxRate saved = repository.save(rate);

        assertThat(saved.id().value()).isEqualTo(1L);
        assertThat(saved.buy()).isEqualByComparingTo("1300");
    }

    @Test
    void save_existingRecord_upsertsInPlace() {
        FxRate rateV1 = new FxRate(null, DATE, FxView.MEP, new BigDecimal("1300"), new BigDecimal("1310"), FxRateSource.IOL_SYNTHETIC, NOW);
        FxRateJpaEntity existingEntity = FxRateJpaEntity.builder()
                .id(10L).rateDate(DATE).fxView(FxView.MEP)
                .buy(new BigDecimal("1300")).sell(new BigDecimal("1310"))
                .source(FxRateSource.IOL_SYNTHETIC).createdAt(NOW).build();

        given(jpaRepository.findByRateDateAndFxView(DATE, FxView.MEP)).willReturn(Optional.of(existingEntity));
        given(jpaRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        FxRate rateV2 = new FxRate(null, DATE, FxView.MEP, new BigDecimal("1320"), new BigDecimal("1330"), FxRateSource.IOL_SYNTHETIC, NOW);
        FxRate saved = repository.save(rateV2);

        assertThat(saved.id().value()).isEqualTo(10L);
        assertThat(saved.buy()).isEqualByComparingTo("1320");
        assertThat(saved.sell()).isEqualByComparingTo("1330");
    }

    @Test
    void findByDate_delegatesToJpa() {
        FxRateJpaEntity entity = FxRateJpaEntity.builder()
                .id(1L).rateDate(DATE).fxView(FxView.CCL)
                .buy(new BigDecimal("1400")).sell(new BigDecimal("1410"))
                .source(FxRateSource.IOL_SYNTHETIC).createdAt(NOW).build();
        given(jpaRepository.findByRateDateAndFxView(DATE, FxView.CCL)).willReturn(Optional.of(entity));

        Optional<FxRate> result = repository.findByDate(DATE, FxView.CCL);

        assertThat(result).isPresent();
        assertThat(result.get().view()).isEqualTo(FxView.CCL);
    }

    @Test
    void findByDateBetween_delegatesToJpa() {
        LocalDate from = DATE.minusDays(5);
        LocalDate to = DATE;

        FxRateJpaEntity entity1 = FxRateJpaEntity.builder()
                .id(1L).rateDate(from).fxView(FxView.OFICIAL)
                .buy(new BigDecimal("900")).sell(new BigDecimal("920"))
                .source(FxRateSource.IOL_DIRECT).createdAt(NOW).build();

        given(jpaRepository.findByRateDateBetweenAndFxViewOrderByRateDateAsc(from, to, FxView.OFICIAL))
                .willReturn(List.of(entity1));

        List<FxRate> result = repository.findByDateBetween(from, to, FxView.OFICIAL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(from);
    }

    @Test
    void findLatest_delegatesToJpa() {
        FxRateJpaEntity entity = FxRateJpaEntity.builder()
                .id(5L).rateDate(DATE).fxView(FxView.MEP)
                .buy(new BigDecimal("1350")).sell(new BigDecimal("1360"))
                .source(FxRateSource.IOL_SYNTHETIC).createdAt(NOW).build();
        given(jpaRepository.findFirstByFxViewOrderByRateDateDesc(FxView.MEP)).willReturn(Optional.of(entity));

        Optional<FxRate> result = repository.findLatest(FxView.MEP);

        assertThat(result).isPresent();
        assertThat(result.get().id().value()).isEqualTo(5L);
    }
}
