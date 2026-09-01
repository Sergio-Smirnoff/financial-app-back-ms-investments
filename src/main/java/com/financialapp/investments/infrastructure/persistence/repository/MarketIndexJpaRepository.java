package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.infrastructure.persistence.entity.MarketIndexJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketIndexJpaRepository extends JpaRepository<MarketIndexJpaEntity, String> {
}
