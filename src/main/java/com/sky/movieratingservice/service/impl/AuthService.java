package com.sky.movieratingservice.service.impl;

import com.sky.movieratingservice.api.dto.request.UserLoginRequestDto;
import com.sky.movieratingservice.api.dto.request.UserRegistrationRequestDto;
import com.sky.movieratingservice.api.dto.response.AuthResponseDto;
import com.sky.movieratingservice.domain.entity.User;
import com.sky.movieratingservice.domain.repository.UserRepository;
import com.sky.movieratingservice.mapper.UserMapper;
import com.sky.movieratingservice.security.JwtGenerator;
import com.sky.movieratingservice.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final UserMapper userMapper;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationInMs;


    @Override
    @Transactional
    public AuthResponseDto register(UserRegistrationRequestDto userRegistrationRequestDto) {
        log.info("Registering user with email: {}", userRegistrationRequestDto.getEmail());

        if (userRepository.existsByEmail(userRegistrationRequestDto.getEmail())) {
            log.warn("Registration failed: Email {} is already in use.", userRegistrationRequestDto.getEmail());
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = User.builder()
                .email(userRegistrationRequestDto.getEmail())
                .password(passwordEncoder.encode(userRegistrationRequestDto.getPassword()))
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully {}", user);
        String token = jwtGenerator.generateJwtToken(user.getEmail());
        return AuthResponseDto.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .expiresIn(jwtExpirationInMs / 1000) // Convert milliseconds to seconds
                .userResponseDto(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDto login(UserLoginRequestDto userLoginRequestDto) {
        log.info("login request  with email: {}", userLoginRequestDto.getEmail());
        User user = userRepository.findByEmail(userLoginRequestDto.getEmail()) // change getId() if necessary
                .orElseThrow(() -> new IllegalArgumentException(String.format("User not found with Email: {} ", userLoginRequestDto.getEmail())));
        if(!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword())){
            log.warn("Login failed: Invalid credentials");
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtGenerator.generateJwtToken(user.getEmail());

        return AuthResponseDto.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationInMs / 1000) // Convert milliseconds to seconds
                .userResponseDto(userMapper.toUserResponse(user))
                .build();
    }
}
