package com.financialapp.investments.infrastructure.persistence.jpa;

import com.financialapp.investments.infrastructure.persistence.entity.MarketPanelQuoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarketPanelQuoteJpaRepository extends JpaRepository<MarketPanelQuoteJpaEntity, String> {

    Optional<MarketPanelQuoteJpaEntity> findByTicker(String ticker);
}
