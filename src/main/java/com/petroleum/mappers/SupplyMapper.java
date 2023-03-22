package com.petroleum.mappers;

import com.petroleum.models.Supply;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface SupplyMapper {
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "date", ignore = true)
    void update(@MappingTarget Supply supply, Supply supplyDto);
}
