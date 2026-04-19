package com.financialapp.investments.model.entity;

import com.financialapp.investments.model.enums.AssetType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "holdings", schema = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "bank_account_id")
    private Long bankAccountId;

    @Column(nullable = false, length = 20)
    private String ticker;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    private AssetType assetType;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal quantity;

    @Column(name = "avg_purchase_price", nullable = false, precision = 18, scale = 6)
    private BigDecimal avgPurchasePrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "notify_gain_threshold_pct", precision = 5, scale = 2)
    private BigDecimal notifyGainThresholdPct;

    @Column(name = "notify_loss_threshold_pct", precision = 5, scale = 2)
    private BigDecimal notifyLossThresholdPct;

    @Column(name = "last_gain_notified_at")
    private LocalDateTime lastGainNotifiedAt;

    @Column(name = "last_loss_notified_at")
    private LocalDateTime lastLossNotifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
