package com.financialapp.investments.domain.repository;

import com.financialapp.investments.domain.model.refresh.RefreshJob;

import java.util.Optional;

public interface RefreshJobRepository {

    RefreshJob save(RefreshJob job);

    Optional<RefreshJob> findLatestInProgress();
}
