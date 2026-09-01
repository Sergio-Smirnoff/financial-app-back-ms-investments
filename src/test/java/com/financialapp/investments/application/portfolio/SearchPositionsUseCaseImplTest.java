package com.financialapp.investments.application.portfolio;

import com.financialapp.investments.application.portfolio.impl.SearchPositionsUseCaseImpl;
import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.*;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.HoldingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchPositionsUseCaseImplTest {

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private SearchPositionsUseCaseImpl useCase;

    private static final UserId USER = new UserId(3L);

    @Test
    void matchesTickerPrefixAndNameSubstring() {
        when(holdingRepository.findByUserId(USER))
                .thenReturn(List.of(holding("YPFD", "YPF S.A."), holding("AL30", "Bonar 2030")));

        List<Holding> found = useCase.execute(USER, "ypf");

        assertThat(found).extracting(h -> h.ticker().value()).containsExactly("YPFD");
    }

    @Test
    void matchesByNameSubstringCaseInsensitive() {
        when(holdingRepository.findByUserId(USER))
                .thenReturn(List.of(holding("YPFD", "YPF S.A."), holding("AL30", "Bonar 2030")));

        List<Holding> found = useCase.execute(USER, "bonar");

        assertThat(found).extracting(h -> h.ticker().value()).containsExactly("AL30");
    }

    @Test
    void returnsEmptyListWhenQueryIsBlankOrNull() {
        assertThat(useCase.execute(USER, null)).isEmpty();
        assertThat(useCase.execute(USER, "   ")).isEmpty();
    }

    @Test
    void returnsEmptyListWhenNoMatchesFound() {
        when(holdingRepository.findByUserId(USER))
                .thenReturn(List.of(holding("YPFD", "YPF S.A.")));

        List<Holding> found = useCase.execute(USER, "ggal");

        assertThat(found).isEmpty();
    }

    private static Holding holding(String ticker, String name) {
        return new Holding(
                new HoldingId(1L),
                USER,
                new BankNumber("007"),
                new Ticker(ticker),
                name,
                AssetType.STOCK,
                new HoldingQuantity(new BigDecimal("10")),
                Money.of(new BigDecimal("100"), "ARS"),
                ThresholdConfig.disabled(),
                NotificationTimestamps.empty(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
