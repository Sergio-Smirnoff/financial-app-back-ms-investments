package com.financialapp.investments.infrastructure.persistence.jpa;

import com.financialapp.investments.domain.model.fx.FxView;
import com.financialapp.investments.infrastructure.persistence.entity.FxRateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FxRateJpaRepository extends JpaRepository<FxRateJpaEntity, Long> {

    Optional<FxRateJpaEntity> findByRateDateAndFxView(LocalDate rateDate, FxView fxView);

    List<FxRateJpaEntity> findByRateDateBetweenAndFxViewOrderByRateDateAsc(LocalDate from, LocalDate to, FxView fxView);

    Optional<FxRateJpaEntity> findFirstByFxViewOrderByRateDateDesc(FxView fxView);
}
