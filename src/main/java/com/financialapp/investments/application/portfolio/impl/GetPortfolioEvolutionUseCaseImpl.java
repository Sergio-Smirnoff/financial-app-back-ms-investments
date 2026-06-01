package com.financialapp.investments.application.portfolio.impl;

import com.financialapp.investments.domain.repository.PortfolioSnapshotRepository;
import com.financialapp.investments.domain.usecase.portfolio.GetPortfolioEvolutionUseCase;
import com.financialapp.investments.domain.usecase.portfolio.command.GetPortfolioEvolutionCommand;
import com.financialapp.investments.domain.usecase.portfolio.response.PortfolioEvolutionPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPortfolioEvolutionUseCaseImpl implements GetPortfolioEvolutionUseCase {

    private final PortfolioSnapshotRepository snapshotRepository;

    @Override
    public List<PortfolioEvolutionPoint> execute(GetPortfolioEvolutionCommand command) {
        LocalDate from = LocalDate.now().minusDays(command.days());
        return snapshotRepository.findByUserIdAndSnapshotDateAfter(command.userId(), from)
                .stream()
                .map(s -> new PortfolioEvolutionPoint(s.snapshotDate(), s.totals()))
                .toList();
    }
}
