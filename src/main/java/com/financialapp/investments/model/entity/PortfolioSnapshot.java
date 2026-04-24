package com.financialapp.investments.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_snapshots", schema = "investments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private LocalDate snapshotDate;
    private BigDecimal totalValueArs;
    private BigDecimal totalValueUsd;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
