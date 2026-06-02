package com.financialapp.investments.web.controller;

import com.financialapp.investments.domain.model.history.AssetPriceHistory;
import com.financialapp.investments.domain.usecase.price.GetPriceHistoryUseCase;
import com.financialapp.investments.domain.usecase.price.command.GetPriceHistoryCommand;
import com.financialapp.investments.web.mapper.PriceWebMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceHistoryControllerTest {

    @Mock private GetPriceHistoryUseCase getPriceHistoryUseCase;
    @Mock private PriceWebMapper priceWebMapper;
    @InjectMocks private PriceHistoryController controller;

    @Test
    void getHistory_nullFromTo_appliesWideWindow() {
        when(getPriceHistoryUseCase.execute(any())).thenReturn(List.<AssetPriceHistory>of());
        LocalDateTime before = LocalDateTime.now();

        controller.getHistory("GGAL", null, null);

        LocalDateTime after = LocalDateTime.now();
        ArgumentCaptor<GetPriceHistoryCommand> cap = ArgumentCaptor.forClass(GetPriceHistoryCommand.class);
        verify(getPriceHistoryUseCase).execute(cap.capture());
        GetPriceHistoryCommand cmd = cap.getValue();

        assertThat(cmd.ticker().value()).isEqualTo("GGAL");
        // to ≈ now
        assertThat(cmd.to()).isBetween(before, after);
        // from ≈ now - 10y (wide window)
        assertThat(cmd.from()).isBetween(before.minusYears(10), after.minusYears(10));
        assertThat(cmd.from()).isBefore(cmd.to());
    }

    @Test
    void getHistory_explicitFromTo_passedThrough() {
        when(getPriceHistoryUseCase.execute(any())).thenReturn(List.<AssetPriceHistory>of());
        LocalDateTime from = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 1, 0, 0);

        controller.getHistory("GGAL", from, to);

        ArgumentCaptor<GetPriceHistoryCommand> cap = ArgumentCaptor.forClass(GetPriceHistoryCommand.class);
        verify(getPriceHistoryUseCase).execute(cap.capture());
        assertThat(cap.getValue().from()).isEqualTo(from);
        assertThat(cap.getValue().to()).isEqualTo(to);
    }
}
