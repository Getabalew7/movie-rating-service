package com.sky.movieratingservice.service;

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
import com.sky.movieratingservice.security.UserPrincipal;
import com.sky.movieratingservice.service.impl.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private UserRegistrationRequestDto registerRequest;
    private UserLoginRequestDto loginRequest;
    private User user;
    private UserResponseDto userResponse;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        // Set JWT expiration time using reflection
        ReflectionTestUtils.setField(authService, "jwtExpirationInMs", 86400000L);

        // Setup test data
        registerRequest = UserRegistrationRequestDto.builder()
                .email("test@example.com")
                .password("Test123!@#")
                .build();

        loginRequest = UserLoginRequestDto.builder()
                .email("test@example.com")
                .password("Test123!@#")
                .build();

        UUID userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@example.com")
                .password("$2a$12$encodedPassword")
                .build();

        userResponse = UserResponseDto.builder()
                .id(userId)
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .build();

        userPrincipal = UserPrincipal.create(user);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$12$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt.token.here");
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // When
        AuthResponseDto response = authService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt.token.here");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(86400L);
        assertThat(response.getUserResponseDto()).isNotNull();
        assertThat(response.getUserResponseDto().getEmail()).isEqualTo("test@example.com");

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void shouldThrowExceptionWhenRegisteringWithExistingEmail() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists")
                .hasMessageContaining("test@example.com");

        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void shouldRegisterAndEncodePassword() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$12$encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertThat(savedUser.getPassword()).isEqualTo("$2a$12$encodedPassword");
            assertThat(savedUser.getPassword()).isNotEqualTo(registerRequest.getPassword());
            return user;
        });
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(any())).thenReturn("token");
        when(userMapper.toUserResponse(any())).thenReturn(userResponse);

        // When
        authService.register(registerRequest);

        // Then
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getPassword().equals("$2a$12$encodedPassword")
        ));
    }

    @Test
    void shouldThrowExceptionWhenLoginWithInvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider, never()).generateToken(any());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void shouldThrowExceptionWhenLoginUserNotFoundAfterAuthentication() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found")
                .hasMessageContaining("test@example.com");

        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    void shouldGetCurrentUserSuccessfully() {
        // Given
        UUID userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // When
        UserResponseDto response = authService.getCurrentUser(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        verify(userRepository).findById(userId);
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void shouldThrowExceptionWhenGetCurrentUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.getCurrentUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found")
                .hasMessageContaining(userId.toString());

        verify(userRepository).findById(userId);
        verify(userMapper, never()).toUserResponse(any());
    }

    @Test
    void shouldGenerateTokenWithCorrectExpiration() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("token");
        when(userMapper.toUserResponse(any())).thenReturn(userResponse);

        // When
        AuthResponseDto response = authService.register(registerRequest);

        // Then
        assertThat(response.getExpiresIn()).isEqualTo(86400L); // 24 hours in seconds
    }

    @Test
    void shouldSaveUserWithCorrectEmail() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertThat(savedUser.getEmail()).isEqualTo(registerRequest.getEmail());
            return user;
        });
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(any())).thenReturn("token");
        when(userMapper.toUserResponse(any())).thenReturn(userResponse);

        // When
        authService.register(registerRequest);

        // Then
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getEmail().equals(registerRequest.getEmail())
        ));
    }

    @Test
    void shouldReturnTokenTypeBearer() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(any())).thenReturn("token");
        when(userMapper.toUserResponse(any())).thenReturn(userResponse);

        // When
        AuthResponseDto response = authService.register(registerRequest);

        // Then
        assertThat(response.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void shouldHandleNullTokenGracefully() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(any())).thenReturn(null);
        when(userMapper.toUserResponse(any())).thenReturn(userResponse);

        // When
        AuthResponseDto response = authService.register(registerRequest);

        // Then
        assertThat(response.getAccessToken()).isNull();
    }

    @Test
    void shouldMapUserToResponseCorrectly() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(any())).thenReturn("token");
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        // When
        AuthResponseDto response = authService.register(registerRequest);

        // Then
        assertThat(response.getUserResponseDto()).isEqualTo(userResponse);
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void shouldVerifyMethodCallOrderDuringRegistration() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(any())).thenReturn("token");
        when(userMapper.toUserResponse(any())).thenReturn(userResponse);

        // When
        authService.register(registerRequest);

        // Then - Verify order of operations
        var inOrder = inOrder(
                userRepository,
                passwordEncoder,
                authenticationManager,
                tokenProvider,
                userMapper
        );

        inOrder.verify(userRepository).existsByEmail(anyString());
        inOrder.verify(passwordEncoder).encode(anyString());
        inOrder.verify(userRepository).save(any(User.class));
        inOrder.verify(authenticationManager).authenticate(any());
        inOrder.verify(tokenProvider).generateToken(any());
        inOrder.verify(userMapper).toUserResponse(any());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenPasswordDoesNotMatch() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Invalid credentials");

        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(tokenProvider, never()).generateToken(any());
    }
}