package com.sky.movieratingservice.service;

import com.sky.movieratingservice.api.dto.request.UserLoginRequestDto;
import com.sky.movieratingservice.api.dto.request.UserRegistrationRequestDto;
import com.sky.movieratingservice.api.dto.response.AuthResponseDto;
import jakarta.validation.Valid;

public interface IAuthService {
    AuthResponseDto register(@Valid UserRegistrationRequestDto userRegistrationRequestDto);

    AuthResponseDto login(@Valid UserLoginRequestDto userLoginRequestDto);
}
