package com.financialapp.investments.domain.repository;

import com.financialapp.investments.domain.common.model.PageRequest;
import com.financialapp.investments.domain.common.model.PageResult;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.HoldingFilter;
import com.financialapp.investments.domain.model.holding.HoldingId;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository {

    Holding save(Holding holding);

    List<Holding> saveAll(List<Holding> holdings);

    Optional<Holding> findByIdAndUserId(HoldingId id, UserId userId);

    PageResult<Holding> findFiltered(HoldingFilter filter, PageRequest pageRequest);

    List<Holding> findByUserId(UserId userId);

    void delete(HoldingId id);
}
