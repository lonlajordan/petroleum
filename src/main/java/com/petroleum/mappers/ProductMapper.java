package com.petroleum.mappers;

import com.petroleum.models.Product;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ProductMapper {
    @Mapping(target = "volume", ignore = true)
    @Mapping(target = "invoices", ignore = true)
    @Mapping(target = "supplies", ignore = true)
    void update(@MappingTarget Product product, Product productDto);
}
