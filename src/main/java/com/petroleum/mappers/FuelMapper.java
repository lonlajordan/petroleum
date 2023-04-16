package com.petroleum.mappers;

import com.petroleum.models.Fuel;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface FuelMapper {
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "date", ignore = true)
    void update(@MappingTarget Fuel fuel, Fuel fuelDto);
}