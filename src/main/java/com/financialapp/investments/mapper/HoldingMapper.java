package com.financialapp.investments.mapper;

import com.financialapp.investments.model.dto.response.HoldingResponse;
import com.financialapp.investments.model.entity.Holding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HoldingMapper {

    @Mapping(target = "assetType", expression = "java(holding.getAssetType().name())")
    HoldingResponse toResponse(Holding holding);
}
