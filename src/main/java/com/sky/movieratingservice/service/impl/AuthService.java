package com.sky.movieratingservice.service.impl;

import com.sky.movieratingservice.api.dto.request.UserLoginRequestDto;
import com.sky.movieratingservice.api.dto.request.UserRegistrationRequestDto;
import com.sky.movieratingservice.api.dto.response.AuthResponseDto;
import com.sky.movieratingservice.api.dto.response.UserResponseDto;
import com.sky.movieratingservice.domain.entity.User;
import com.sky.movieratingservice.domain.exception.DuplicateResourceException;
import com.sky.movieratingservice.domain.exception.ForbiddenException;
import com.sky.movieratingservice.domain.exception.ResourceNotFoundException;
import com.sky.movieratingservice.domain.repository.UserRepository;
import com.sky.movieratingservice.mapper.UserMapper;
import com.sky.movieratingservice.security.JwtTokenProvider;
import com.sky.movieratingservice.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService {
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationInMs;


    @Override
    @Transactional
    public AuthResponseDto register(UserRegistrationRequestDto userRegistrationRequestDto) {
        logger.info("Registering user with email: {}", userRegistrationRequestDto.getEmail());

        if (userRepository.existsByEmail(userRegistrationRequestDto.getEmail())) {
            throw new DuplicateResourceException("User", "email", userRegistrationRequestDto.getEmail());
        }

        User user = User.builder()
                .email(userRegistrationRequestDto.getEmail())
                .password(passwordEncoder.encode(userRegistrationRequestDto.getPassword()))
                .build();

        user = userRepository.save(user);
        logger.info("User registered successfully {}", user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userRegistrationRequestDto.getEmail(),
                        userRegistrationRequestDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponseDto.builder()
                .accessToken(token)
                .expiresIn(jwtExpirationInMs / 1000) // Convert milliseconds to seconds
                .userResponseDto(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponseDto login(UserLoginRequestDto userLoginRequestDto) {
        log.info("login request  with email: {}", userLoginRequestDto.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginRequestDto.getEmail(),
                        userLoginRequestDto.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);



        User user = userRepository.findByEmail(userLoginRequestDto.getEmail()) // change getId() if necessary
                .orElseThrow(() ->  new ResourceNotFoundException("USER","email", userLoginRequestDto.getEmail()));

        if(!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword())){
            throw new ForbiddenException("Invalid credentials");
        }
        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponseDto.builder()
                .accessToken(token)
                .expiresIn(jwtExpirationInMs / 1000) // Convert milliseconds to seconds
                .userResponseDto(userMapper.toUserResponse(user))
                .build();
    }
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return userMapper.toUserResponse(user);
    }
}
