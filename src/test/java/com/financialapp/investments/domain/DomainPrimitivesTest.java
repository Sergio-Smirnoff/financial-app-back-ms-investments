package com.financialapp.investments.domain;

import com.financialapp.investments.domain.common.model.PageRequest;
import com.financialapp.investments.domain.common.model.PageResult;
import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.common.model.Cbu;
import com.financialapp.investments.domain.event.Direction;
import com.financialapp.commons.core.error.ErrorCategory;
import com.financialapp.investments.domain.exception.DomainError;
import com.financialapp.investments.domain.model.holding.HoldingFilter;
import com.financialapp.investments.domain.model.holding.HoldingId;
import com.financialapp.investments.domain.model.history.AssetPriceHistoryId;
import com.financialapp.investments.domain.model.price.AssetPriceId;
import com.financialapp.investments.domain.model.price.AssetType;
import com.financialapp.investments.domain.model.refresh.RefreshJobId;
import com.financialapp.investments.domain.model.refresh.RefreshJobStatus;
import com.financialapp.investments.domain.model.snapshot.PortfolioSnapshotId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainPrimitivesTest {

    @Test
    void userId_null_throws() {
        assertThatThrownBy(() -> new UserId(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void userId_valid() {
        assertThat(new UserId(7L).value()).isEqualTo(7L);
    }

    @Test
    void pageRequest_negativePage_throws() {
        assertThatThrownBy(() -> new PageRequest(-1, 10)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pageRequest_zeroSize_throws() {
        assertThatThrownBy(() -> new PageRequest(0, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pageRequest_valid() {
        PageRequest r = new PageRequest(2, 50);
        assertThat(r.page()).isEqualTo(2);
        assertThat(r.size()).isEqualTo(50);
    }

    @Test
    void pageResult_nullContent_becomesEmpty() {
        PageResult<String> r = new PageResult<>(null, 0, 10, 0, 0);
        assertThat(r.content()).isEmpty();
    }

    @Test
    void pageResult_copiesContent() {
        PageResult<String> r = new PageResult<>(List.of("a"), 0, 10, 1, 1);
        assertThat(r.content()).containsExactly("a");
        assertThat(r.page()).isZero();
        assertThat(r.size()).isEqualTo(10);
        assertThat(r.totalElements()).isOne();
        assertThat(r.totalPages()).isOne();
    }

    @Test
    void direction_valuesAndValueOf() {
        assertThat(Direction.values()).containsExactly(Direction.GAIN, Direction.LOSS);
        assertThat(Direction.valueOf("GAIN")).isEqualTo(Direction.GAIN);
    }

    @Test
    void domainError_exposesCategoryAndCode() {
        assertThat(DomainError.RESOURCE_NOT_FOUND.category()).isEqualTo(ErrorCategory.NOT_FOUND);
        assertThat(DomainError.RESOURCE_NOT_FOUND.code()).isEqualTo("resource_not_found");
        assertThat(DomainError.HOLDING_QUANTITY_INVALID.category()).isEqualTo(ErrorCategory.UNPROCESSABLE);
        assertThat(DomainError.INTERNAL_ERROR.category()).isEqualTo(ErrorCategory.INTERNAL_SERVER_ERROR);
        for (DomainError e : DomainError.values()) {
            assertThat(e.code()).isNotBlank();
        }
    }

    @Test
    void simpleIdRecords_construct() {
        assertThat(new HoldingId(1L).value()).isEqualTo(1L);
        assertThat(new AssetPriceId(2L).value()).isEqualTo(2L);
        assertThat(new AssetPriceHistoryId(3L).value()).isEqualTo(3L);
        assertThat(new RefreshJobId(4L).value()).isEqualTo(4L);
        assertThat(new PortfolioSnapshotId(5L).value()).isEqualTo(5L);
    }

    @Test
    void cbu_invalid_throws() {
        assertThatThrownBy(() -> new Cbu(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Cbu("123")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cbu_valid() {
        assertThat(new Cbu("0070009000000000000010").value()).isEqualTo("0070009000000000000010");
    }

    @Test
    void assetType_enumValues() {
        assertThat(AssetType.values()).containsExactlyInAnyOrder(
                AssetType.STOCK, AssetType.BOND, AssetType.CEDEAR, AssetType.FCI);
        assertThat(AssetType.valueOf("STOCK")).isEqualTo(AssetType.STOCK);
    }

    @Test
    void refreshJobStatus_enumValues() {
        assertThat(RefreshJobStatus.values()).containsExactlyInAnyOrder(
                RefreshJobStatus.PENDING, RefreshJobStatus.IN_PROGRESS,
                RefreshJobStatus.INTERRUPTED, RefreshJobStatus.COMPLETED);
        assertThat(RefreshJobStatus.valueOf("PENDING")).isEqualTo(RefreshJobStatus.PENDING);
    }

    @Test
    void holdingFilter_nullUserId_throws() {
        assertThatThrownBy(() -> new HoldingFilter(null, AssetType.STOCK))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void holdingFilter_nullAssetType_ok_andFlagFalse() {
        HoldingFilter f = new HoldingFilter(new UserId(1L), null);
        assertThat(f.hasAssetTypeFilter()).isFalse();
    }

    @Test
    void holdingFilter_withAssetType_flagTrue() {
        HoldingFilter f = new HoldingFilter(new UserId(1L), AssetType.STOCK);
        assertThat(f.hasAssetTypeFilter()).isTrue();
    }
}
