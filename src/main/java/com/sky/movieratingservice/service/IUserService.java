package com.sky.movieratingservice.service;

import com.sky.movieratingservice.api.dto.response.UserResponseDto;

public interface IUserService {

    UserResponseDto getUserById(java.util.UUID id);
    UserResponseDto getUserByEmail(String email);
}
