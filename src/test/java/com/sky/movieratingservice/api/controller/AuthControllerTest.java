package com.sky.movieratingservice.api.controller;

import com.sky.movieratingservice.api.dto.request.UserLoginRequestDto;
import com.sky.movieratingservice.api.dto.request.UserRegistrationRequestDto;
import com.sky.movieratingservice.common.AbstractIntegrationTest;
import com.sky.movieratingservice.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
class AuthControllerTest extends AbstractIntegrationTest {

    @Test
    void ShouldRegisterSuccessfully()  {
        UserRegistrationRequestDto request = UserRegistrationRequestDto.builder()
                .password("SecurePass123!@")
                .email("newuser@user.com")
                .build();

        webClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accessToken").exists()
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .jsonPath("$.expiresIn").isNumber()
                .jsonPath("$.userResponseDto.email").isEqualTo("newuser@user.com")
                .jsonPath("$.userResponseDto.id").exists();
    }

    @Test
    void shouldFailRegistrationWithDuplicateEmail()  {

        // Given: User with email already exists
        User user = User.builder()
                .email("test@test.com")
                .password("SomePassword123!@")
                .build();
        userRepository.save(user);

        UserRegistrationRequestDto request = UserRegistrationRequestDto.builder()
                .password("AnotherPass123!@")
                .email("test@test.com")
                .build();

        // When & Then
        webClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.message").value(msg -> {
                    assert ((String) msg).contains("already exists");
                });
    }

    @Test
    void shouldFailRegistrationWithWeakPassword()  {
        UserRegistrationRequestDto request = UserRegistrationRequestDto.builder()
                .email("weakpass@example.com")
                .password("weak")
                .build();

        webClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.validationErrors").isArray()
                .jsonPath("$.validationErrors[*].field").value(fields ->{
                    assert ((List<?>) fields).contains("password");
                });
    }

    @Test
    void shouldLoginSuccessfully()  {
        // Given: User exists
        User user = User.builder()
                .email("login@example.com")
                .password(passwordEncoder.encode("Password123!@"))
                .build();
        userRepository.save(user);

        UserLoginRequestDto request = UserLoginRequestDto.builder()
                .email("login@example.com")
                .password("Password123!@")
                .build();

        // When & Then
        webClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").exists()
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .jsonPath("$.expiresIn").isNumber()
                .jsonPath("$.userResponseDto.email").isEqualTo("login@example.com");
    }

    @Test
    void shouldFailLoginWithInvalidPassword()  {
        // Given: User exists
        User user = User.builder()
                .email("wrongpass@example.com")
                .password(passwordEncoder.encode("CorrectPass123!@"))
                .build();
        userRepository.save(user);

        UserLoginRequestDto request = UserLoginRequestDto.builder()
                .email("wrongpass@example.com")
                .password("WrongPassword123!@")
                .build();

        // When & Then
        webClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid email or password");
    }

    @Test
    void shouldFailLoginWithNonExistentUser()  {
        UserLoginRequestDto request = UserLoginRequestDto.builder()
                .email("nonexistent@example.com")
                .password("Password123!@")
                .build();

        webClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
