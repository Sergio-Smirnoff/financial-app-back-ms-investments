package com.financialapp.investments.repository;

import com.financialapp.investments.model.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {

    List<Holding> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Holding> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT DISTINCT h.ticker FROM Holding h")
    List<String> findDistinctTickers();
}
