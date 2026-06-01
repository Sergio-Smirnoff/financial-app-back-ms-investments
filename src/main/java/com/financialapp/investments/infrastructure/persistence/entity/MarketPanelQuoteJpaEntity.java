package com.financialapp.investments.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_panel_quotes", schema = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketPanelQuoteJpaEntity {

    @Id
    private String ticker;

    @Column(name = "last_price", nullable = false)
    private BigDecimal lastPrice;

    @Column(length = 3)
    private String currency;

    private BigDecimal variation;

    private BigDecimal volume;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
