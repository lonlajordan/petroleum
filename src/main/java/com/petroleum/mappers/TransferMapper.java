package com.petroleum.mappers;


import com.petroleum.models.Transfer;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface TransferMapper {
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "loadingDepot", ignore = true)
    @Mapping(target = "deliveryPlace", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "step", ignore = true)
    @Mapping(target = "reason", ignore = true)
    void update(@MappingTarget Transfer transfer, Transfer transferDto);
}
