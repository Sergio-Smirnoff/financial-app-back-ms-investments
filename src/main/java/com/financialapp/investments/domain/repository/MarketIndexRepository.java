package com.financialapp.investments.domain.repository;

import com.financialapp.investments.domain.model.market.MarketIndex;

import java.util.List;
import java.util.Optional;

public interface MarketIndexRepository {
    MarketIndex save(MarketIndex index);
    Optional<MarketIndex> findByCode(String code);
    List<MarketIndex> findAll();
}
