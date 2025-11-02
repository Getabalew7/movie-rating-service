package com.sky.movieratingservice.service;

import com.sky.movieratingservice.api.dto.response.UserResponseDto;
import com.sky.movieratingservice.domain.entity.User;
import com.sky.movieratingservice.domain.exception.ResourceNotFoundException;
import com.sky.movieratingservice.domain.repository.UserRepository;
import com.sky.movieratingservice.mapper.UserMapper;
import com.sky.movieratingservice.service.impl.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponseDto userResponseDto;
    private UUID userId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userEmail = "test@example.com";

        user = User.builder()
                .id(userId)
                .email(userEmail)
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(userId)
                .email(userEmail)
                .build();
    }

    // ===== getUserById =====
    @Test
    void shouldGetUserByIdSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo(userEmail);

        verify(userRepository).findById(userId);
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundById() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining(userId.toString());

        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper);
    }

    // ===== getUserByEmail =====
    @Test
    void shouldGetUserByEmailSuccessfully() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUserByEmail(userEmail);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(userEmail);
        assertThat(result.getId()).isEqualTo(userId);

        verify(userRepository).findByEmail(userEmail);
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail(userEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining(userEmail);

        verify(userRepository).findByEmail(userEmail);
        verifyNoInteractions(userMapper);
    }
}
