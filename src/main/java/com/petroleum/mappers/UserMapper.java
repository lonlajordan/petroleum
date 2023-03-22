package com.petroleum.mappers;

import com.petroleum.models.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "password", ignore = true)
    void update(@MappingTarget User user, User userDto);
}
