package com.financialapp.investments.application.holding;

import com.financialapp.investments.application.holding.impl.ListHoldingsUseCaseImpl;
import com.financialapp.investments.domain.common.model.PageRequest;
import com.financialapp.investments.domain.common.model.PageResult;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.HoldingFilter;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.repository.HoldingRepository;
import com.financialapp.investments.domain.usecase.holding.command.ListHoldingsCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListHoldingsUseCaseImplTest {

    @Mock private HoldingRepository holdingRepository;
    @InjectMocks private ListHoldingsUseCaseImpl useCase;

    @Test
    void execute_passesFilterAndPageThrough_andReturnsRepositoryResult() {
        PageRequest pr = new PageRequest(1, 25);
        ListHoldingsCommand cmd = new ListHoldingsCommand(new UserId(1L), AssetType.STOCK, pr);
        PageResult<com.financialapp.investments.domain.model.holding.Holding> expected =
                new PageResult<>(List.of(), 1, 25, 0, 0);
        when(holdingRepository.findFiltered(any(), any())).thenReturn(expected);

        PageResult<?> actual = useCase.execute(cmd);

        ArgumentCaptor<HoldingFilter> filterCap = ArgumentCaptor.forClass(HoldingFilter.class);
        verify(holdingRepository).findFiltered(filterCap.capture(), any());
        assertThat(filterCap.getValue().userId().value()).isEqualTo(1L);
        assertThat(filterCap.getValue().assetType()).isEqualTo(AssetType.STOCK);
        assertThat(actual).isSameAs(expected);
    }

    private static <T> T any() { return org.mockito.ArgumentMatchers.any(); }
}
