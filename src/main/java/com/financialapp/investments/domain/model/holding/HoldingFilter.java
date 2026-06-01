package com.financialapp.investments.domain.model.holding;

import com.financialapp.investments.domain.common.model.UserId;
import com.financialapp.investments.domain.model.price.AssetType;

import java.util.Objects;

public record HoldingFilter(UserId userId, AssetType assetType) {

    public HoldingFilter {
        Objects.requireNonNull(userId, "userId must not be null");
        // assetType is nullable — null means no filter
    }

    public boolean hasAssetTypeFilter() {
        return assetType != null;
    }
}
