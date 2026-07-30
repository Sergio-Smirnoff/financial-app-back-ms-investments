package com.financialapp.investments.infrastructure.persistence.entity;

import com.financialapp.investments.domain.model.fx.FxRateSource;
import com.financialapp.investments.domain.model.fx.FxView;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fx_rates",
        schema = "investments",
        uniqueConstraints = @UniqueConstraint(name = "uk_fx_rates_date_view", columnNames = {"rate_date", "fx_view"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FxRateJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "fx_view", nullable = false, length = 10)
    private FxView fxView;

    @Column(name = "buy", nullable = false, precision = 12, scale = 4)
    private BigDecimal buy;

    @Column(name = "sell", nullable = false, precision = 12, scale = 4)
    private BigDecimal sell;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private FxRateSource source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
