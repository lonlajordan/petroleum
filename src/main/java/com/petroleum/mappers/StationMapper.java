package com.petroleum.mappers;

import com.petroleum.models.Station;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface StationMapper {
    void update(@MappingTarget Station station, Station model);
}
