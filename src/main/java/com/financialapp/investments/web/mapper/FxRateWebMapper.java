package com.financialapp.investments.web.mapper;

import com.financialapp.investments.domain.model.fx.FxRate;
import com.financialapp.investments.domain.model.fx.FxSnapshotRates;
import com.financialapp.investments.web.dto.response.FxRateResponse;
import com.financialapp.investments.web.dto.response.FxSnapshotRatesResponse;

public final class FxRateWebMapper {

    private FxRateWebMapper() {}

    public static FxRateResponse toResponse(FxRate rate) {
        if (rate == null) {
            return null;
        }
        return new FxRateResponse(
                rate.date(),
                rate.view() != null ? rate.view().name() : null,
                rate.buy() != null ? rate.buy().toPlainString() : null,
                rate.sell() != null ? rate.sell().toPlainString() : null,
                rate.source() != null ? rate.source().name() : null
        );
    }

    public static FxSnapshotRatesResponse toResponse(FxSnapshotRates snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new FxSnapshotRatesResponse(
                snapshot.mepRate() != null ? snapshot.mepRate().toPlainString() : null,
                snapshot.cclRate() != null ? snapshot.cclRate().toPlainString() : null,
                snapshot.oficialRate() != null ? snapshot.oficialRate().toPlainString() : null,
                snapshot.rateDate()
        );
    }
}
