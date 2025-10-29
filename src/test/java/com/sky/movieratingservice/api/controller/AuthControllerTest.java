package com.sky.movieratingservice.api.controller;

import com.sky.movieratingservice.api.dto.request.UserLoginRequestDto;
import com.sky.movieratingservice.api.dto.request.UserRegistrationRequestDto;
import com.sky.movieratingservice.common.AbstractIntegrationTest;
import com.sky.movieratingservice.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends AbstractIntegrationTest {

    @Test
    void ShouldRegisterSuccessfully() throws Exception {
        UserRegistrationRequestDto request = UserRegistrationRequestDto.builder()
                .password("SecurePass123!@")
                .email("newuser@user.com")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.userResponseDto.email").value("newuser@user.com"))
                .andExpect(jsonPath("$.userResponseDto.id").exists());
    }

    @Test
    void shouldFailRegistrationWithDuplicateEmail() throws Exception {

        //Given: User with email already exists
        User user = User.builder()
                .email("test@test.com")
                .password("SomePassword123!@")
                .build();

        userRepository.save(user);

        UserRegistrationRequestDto request = UserRegistrationRequestDto.builder()
                .password("AnotherPass123!@")
                .email("test@test.com")
                .build();

        //When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("409"))
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    void shouldFailRegistrationWithWeakPassword() throws Exception {
        UserRegistrationRequestDto request = UserRegistrationRequestDto.builder()
                .email("weakpass@example.com")
                .password("weak")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[*].field", hasItem("password")));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
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
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.userResponseDto.email").value("login@example.com"));
    }

    @Test
    void shouldFailLoginWithInvalidPassword() throws Exception {
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
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or" +
                        " password"));
    }

    @Test
    void shouldFailLoginWithNonExistentUser() throws Exception {
        UserLoginRequestDto request = UserLoginRequestDto.builder()
                .email("nonexistent@example.com")
                .password("Password123!@")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}