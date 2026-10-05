package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.common.model.BankNumber;
import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.holding.Holding;
import com.financialapp.investments.domain.model.holding.HoldingQuantity;
import com.financialapp.investments.domain.model.holding.ThresholdConfig;
import com.financialapp.investments.domain.model.holding.Ticker;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.infrastructure.persistence.mapper.HoldingPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({HoldingRepositoryImpl.class, HoldingPersistenceMapper.class})
class HoldingRepositoryImplJpaTest {

    private static final UserId OWNER = new UserId(1L);

    @Autowired private HoldingRepositoryImpl repository;
    @Autowired private EntityManager entityManager;

    @Test
    void findByIdAndUserIdForUpdate_readsOnlyTheOwnersHolding() {
        Holding saved = repository.save(Holding.create(OWNER, new BankNumber("007"), new Ticker("GGAL"),
                "Grupo Galicia", AssetType.STOCK, new HoldingQuantity(new BigDecimal("10")),
                Money.of(new BigDecimal("150"), "ARS"), ThresholdConfig.disabled()));
        entityManager.flush();
        entityManager.clear();

        assertThat(repository.findByIdAndUserIdForUpdate(saved.id(), OWNER))
                .hasValueSatisfying(h -> assertThat(h.quantity().value()).isEqualByComparingTo("10"));
        assertThat(repository.findByIdAndUserIdForUpdate(saved.id(), new UserId(2L))).isEmpty();
    }
}
