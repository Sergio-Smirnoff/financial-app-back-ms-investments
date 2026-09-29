package com.financialapp.investments.infrastructure.persistence.repository;

import com.financialapp.investments.domain.common.model.Money;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshot;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshotId;
import com.financialapp.investments.infrastructure.persistence.mapper.PortfolioSnapshotPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportAutoConfiguration(JacksonAutoConfiguration.class)
@Import({PortfolioSnapshotRepositoryImpl.class, PortfolioSnapshotPersistenceMapper.class})
class PortfolioSnapshotRepositoryImplJpaTest {

    private static final UserId USER = new UserId(1L);
    private static final LocalDate DAY = LocalDate.of(2026, 9, 28);

    @Autowired private PortfolioSnapshotRepositoryImpl repository;
    @Autowired private EntityManager entityManager;

    @Test
    void totalsAreStoredAsAJsonObjectAndReadBack() {
        repository.save(new PortfolioSnapshot(new PortfolioSnapshotId(null), USER, DAY,
                List.of(Money.of(new BigDecimal("904779.00"), "ARS"), Money.of(new BigDecimal("12.50"), "USD")),
                LocalDateTime.of(2026, 9, 28, 3, 0)));
        entityManager.flush();
        entityManager.clear();

        Object stored = entityManager
                .createNativeQuery("SELECT CAST(totals AS VARCHAR) FROM investments.portfolio_snapshots")
                .getSingleResult();
        assertThat(stored.toString()).startsWith("{").contains("\"ARS\"");

        List<PortfolioSnapshot> read = repository.findByUserIdAndSnapshotDateAfter(USER, DAY);
        assertThat(read).singleElement().satisfies(snapshot -> assertThat(snapshot.totals())
                .extracting(m -> m.currency().getCurrencyCode() + "=" + m.amount().stripTrailingZeros().toPlainString())
                .containsExactly("ARS=904779", "USD=12.5"));
    }
}
