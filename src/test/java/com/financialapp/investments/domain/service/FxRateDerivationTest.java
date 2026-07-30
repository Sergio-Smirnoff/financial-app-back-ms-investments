package com.financialapp.investments.domain.service;

import com.financialapp.investments.domain.gateway.FxLegPriceGateway;
import com.financialapp.investments.domain.model.fx.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FxRateDerivationTest {

    @Mock
    private FxLegPriceGateway gateway;

    private FxRateDerivation derivation;
    private static final LocalDate DATE = LocalDate.of(2026, 7, 29);
    private static final FxBondPairs PAIRS = new FxBondPairs("AL30", "AL30D", "AL30", "AL30C");

    @BeforeEach
    void setUp() {
        derivation = new FxRateDerivation(gateway, PAIRS);
    }

    @Test
    void deriveRates_happyPath_computesMepCclAndOficial() {
        given(gateway.priceOn("AL30", DATE)).willReturn(Optional.of(new BigDecimal("68000.00")));
        given(gateway.priceOn("AL30D", DATE)).willReturn(Optional.of(new BigDecimal("50.00")));
        given(gateway.priceOn("AL30C", DATE)).willReturn(Optional.of(new BigDecimal("48.50")));
        given(gateway.oficialRateOn(DATE)).willReturn(Optional.of(new BigDecimal("950.00")));

        FxSnapshotRates snapshot = derivation.deriveRates(DATE);

        assertThat(snapshot.rateDate()).isEqualTo(DATE);
        assertThat(snapshot.mepRate()).isEqualByComparingTo("1360.0000"); // 68000 / 50
        assertThat(snapshot.cclRate()).isEqualByComparingTo("1402.0619"); // 68000 / 48.50
        assertThat(snapshot.oficialRate()).isEqualByComparingTo("950.00");
    }

    @Test
    void deriveRates_missingUsdLeg_mepNullOthersIntact() {
        given(gateway.priceOn("AL30", DATE)).willReturn(Optional.of(new BigDecimal("68000.00")));
        given(gateway.priceOn("AL30D", DATE)).willReturn(Optional.empty());
        given(gateway.priceOn("AL30C", DATE)).willReturn(Optional.of(new BigDecimal("50.00")));
        given(gateway.oficialRateOn(DATE)).willReturn(Optional.of(new BigDecimal("950.00")));

        FxSnapshotRates snapshot = derivation.deriveRates(DATE);

        assertThat(snapshot.mepRate()).isNull();
        assertThat(snapshot.cclRate()).isEqualByComparingTo("1360.0000");
        assertThat(snapshot.oficialRate()).isEqualByComparingTo("950.00");
    }

    @Test
    void deriveRates_zeroUsdLeg_returnsNullWithoutArithmeticException() {
        given(gateway.priceOn("AL30", DATE)).willReturn(Optional.of(new BigDecimal("68000.00")));
        given(gateway.priceOn("AL30D", DATE)).willReturn(Optional.of(BigDecimal.ZERO));
        given(gateway.priceOn("AL30C", DATE)).willReturn(Optional.of(new BigDecimal("50.00")));

        FxSnapshotRates snapshot = derivation.deriveRates(DATE);

        assertThat(snapshot.mepRate()).isNull();
        assertThat(snapshot.cclRate()).isEqualByComparingTo("1360.0000");
    }

    @Test
    void deriveAsFxRates_mapsOnlyNonNullViewsAsSyntheticFxRates() {
        given(gateway.priceOn("AL30", DATE)).willReturn(Optional.of(new BigDecimal("68000.00")));
        given(gateway.priceOn("AL30D", DATE)).willReturn(Optional.of(new BigDecimal("50.00")));
        given(gateway.priceOn("AL30C", DATE)).willReturn(Optional.empty());
        given(gateway.oficialRateOn(DATE)).willReturn(Optional.of(new BigDecimal("950.00")));

        List<FxRate> rates = derivation.deriveAsFxRates(DATE);

        assertThat(rates).hasSize(2);

        FxRate mep = rates.stream().filter(r -> r.view() == FxView.MEP).findFirst().orElseThrow();
        assertThat(mep.buy()).isEqualByComparingTo("1360.0000");
        assertThat(mep.sell()).isEqualByComparingTo("1360.0000");
        assertThat(mep.source()).isEqualTo(FxRateSource.IOL_SYNTHETIC);

        FxRate oficial = rates.stream().filter(r -> r.view() == FxView.OFICIAL).findFirst().orElseThrow();
        assertThat(oficial.buy()).isEqualByComparingTo("950.00");
        assertThat(oficial.sell()).isEqualByComparingTo("950.00");
        assertThat(oficial.source()).isEqualTo(FxRateSource.IOL_SYNTHETIC);
    }
}
