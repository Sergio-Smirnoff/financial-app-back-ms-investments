package com.financialapp.investments.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_indices", schema = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MarketIndexJpaEntity {

    @Id
    @Column(name = "code", length = 20, nullable = false)
    private String code;

    @Column(name = "value", precision = 14, scale = 2, nullable = false)
    private BigDecimal value;

    @Column(name = "variation", precision = 8, scale = 4)
    private BigDecimal variation;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
