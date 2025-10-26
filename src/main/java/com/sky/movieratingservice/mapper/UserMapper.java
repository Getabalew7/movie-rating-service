package com.sky.movieratingservice.mapper;

import com.sky.movieratingservice.api.dto.response.UserResponseDto;
import com.sky.movieratingservice.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserResponseDto toUserResponse(User user);
}
