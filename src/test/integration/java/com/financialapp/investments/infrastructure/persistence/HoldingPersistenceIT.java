package com.financialapp.investments.infrastructure.persistence;

import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.support.AbstractIntegrationIT;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Persistence integration test for the holdings table on H2: exercises the hand-written
 * Spring Data query methods end-to-end against a real schema.
 */
class HoldingPersistenceIT extends AbstractIntegrationIT {

    @Test
    void findByBankAccountId_andCount_scopeToAccount() {
        seedHolding(1L, 55L, "AAPL", AssetType.STOCK, "10", "100", "USD", null, null);
        seedHolding(1L, 55L, "MSFT", AssetType.STOCK, "5", "200", "USD", null, null);
        seedHolding(1L, 66L, "GGAL", AssetType.STOCK, "3", "150", "ARS", null, null);

        assertThat(holdingRepository.findByBankAccountId(55L)).hasSize(2);
        assertThat(holdingRepository.countByBankAccountId(55L)).isEqualTo(2);
        assertThat(holdingRepository.countByBankAccountId(66L)).isEqualTo(1);
    }

    @Test
    void findDistinctTickers_dedupesAcrossHoldings() {
        seedHolding(1L, 55L, "GGAL", AssetType.STOCK, "10", "100", "ARS", null, null);
        seedHolding(2L, 77L, "GGAL", AssetType.STOCK, "5", "120", "ARS", null, null);
        seedHolding(1L, 55L, "AAPL", AssetType.STOCK, "1", "300", "USD", null, null);

        assertThat(holdingRepository.findDistinctTickers())
                .containsExactlyInAnyOrder("GGAL", "AAPL");
    }
}
