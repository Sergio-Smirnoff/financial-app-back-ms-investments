package com.financialapp.investments.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_panel_quotes", schema = "investments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarketPanelQuote {
    @Id
    private String ticker;

    @Column(name = "last_price", nullable = false)
    private BigDecimal lastPrice;

    private BigDecimal variation;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
