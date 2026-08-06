package com.financialapp.investments.application.portfolio.impl;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.usecase.portfolio.SearchPositions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SearchPositionsUseCaseImpl implements SearchPositions {

    private final HoldingRepository holdingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Holding> execute(UserId userId, String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            return List.of();
        }
        return holdingRepository.findByUserId(userId).stream()
                .filter(h -> h.ticker().value().toLowerCase(Locale.ROOT).contains(needle)
                        || h.name().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }
}
