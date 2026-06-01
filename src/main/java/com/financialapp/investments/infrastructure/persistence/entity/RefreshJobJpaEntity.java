package com.financialapp.investments.infrastructure.persistence.entity;

import com.financialapp.investments.domain.model.refresh.RefreshJobStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_jobs", schema = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshJobJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RefreshJobStatus status;

    @Column(name = "all_tickers", nullable = false, columnDefinition = "TEXT")
    private String allTickers;

    @Column(name = "last_success_index", nullable = false)
    private int lastSuccessIndex;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
