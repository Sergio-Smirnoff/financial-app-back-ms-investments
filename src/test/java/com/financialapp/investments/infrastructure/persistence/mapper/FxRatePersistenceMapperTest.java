package com.financialapp.investments.infrastructure.persistence.mapper;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxRateId;
import com.financialapp.investments.domain.model.fx.FxRateSource;
import com.financialapp.investments.domain.model.fx.FxView;
import com.financialapp.investments.infrastructure.persistence.entity.FxRateJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FxRatePersistenceMapperTest {

    private final FxRatePersistenceMapper mapper = new FxRatePersistenceMapper();

    @Test
    void roundTrip_mapsCorrectly() {
        LocalDate date = LocalDate.of(2026, 7, 29);
        LocalDateTime now = LocalDateTime.now();

        FxRate domain = new FxRate(
                new FxRateId(10L),
                date,
                FxView.MEP,
                new BigDecimal("1300.5000"),
                new BigDecimal("1310.0000"),
                FxRateSource.IOL_SYNTHETIC,
                now
        );

        FxRateJpaEntity entity = mapper.toEntity(domain);
        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getRateDate()).isEqualTo(date);
        assertThat(entity.getFxView()).isEqualTo(FxView.MEP);
        assertThat(entity.getBuy()).isEqualByComparingTo("1300.5000");
        assertThat(entity.getSell()).isEqualByComparingTo("1310.0000");
        assertThat(entity.getSource()).isEqualTo(FxRateSource.IOL_SYNTHETIC);
        assertThat(entity.getCreatedAt()).isEqualTo(now);

        FxRate back = mapper.toDomain(entity);
        assertThat(back.id().value()).isEqualTo(10L);
        assertThat(back.date()).isEqualTo(date);
        assertThat(back.view()).isEqualTo(FxView.MEP);
        assertThat(back.buy()).isEqualByComparingTo("1300.5000");
        assertThat(back.sell()).isEqualByComparingTo("1310.0000");
        assertThat(back.source()).isEqualTo(FxRateSource.IOL_SYNTHETIC);
    }

    @Test
    void nullHandling() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDomain(null)).isNull();
    }
}
