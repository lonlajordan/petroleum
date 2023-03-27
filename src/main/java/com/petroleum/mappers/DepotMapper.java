package com.petroleum.mappers;

import com.petroleum.models.Depot;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface DepotMapper {
    @Mapping(target = "stocks", ignore = true)
    void update(@MappingTarget Depot depot, Depot depotDto);
}
