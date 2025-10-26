package com.sky.movieratingservice.service.impl;

import com.sky.movieratingservice.api.dto.request.UserLoginRequestDto;
import com.sky.movieratingservice.api.dto.request.UserRegistrationRequestDto;
import com.sky.movieratingservice.api.dto.response.AuthResponseDto;
import com.sky.movieratingservice.domain.entity.User;
import com.sky.movieratingservice.domain.repository.UserRepository;
import com.sky.movieratingservice.mapper.UserMapper;
import com.sky.movieratingservice.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipal;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationInMs;


    @Override
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

        log.info("Registered user with email: {}", userRegistrationRequestDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userRegistrationRequestDto.getEmail(), userRegistrationRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = "dummy-jwt-token"; // Replace with actual JWT generation logic

        return AuthResponseDto.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .expiresIn(jwtExpirationInMs / 1000) // Convert milliseconds to seconds
                .userResponseDto(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    public AuthResponseDto login(UserLoginRequestDto userLoginRequestDto) {
        log.info("login request  with email: {}", userLoginRequestDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLoginRequestDto.getEmail(), userLoginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findByEmail(userLoginRequestDto.getEmail()) // change getId() if necessary
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = "dummy-j"; // Replace with actual JWT generation logic

        return AuthResponseDto.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationInMs / 1000) // Convert milliseconds to seconds
                .userResponseDto(userMapper.toUserResponse(user))
                .build();
    }
}
