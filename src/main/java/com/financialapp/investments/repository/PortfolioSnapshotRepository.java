package com.financialapp.investments.repository;

import com.financialapp.investments.model.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {
    List<PortfolioSnapshot> findByUserIdAndSnapshotDateGreaterThanEqualOrderBySnapshotDateAsc(Long userId, LocalDate date);
}
