package com.financialapp.investments.repository;

import com.financialapp.investments.model.entity.MarketPanelQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketPanelQuoteRepository extends JpaRepository<MarketPanelQuote, String> {
}
