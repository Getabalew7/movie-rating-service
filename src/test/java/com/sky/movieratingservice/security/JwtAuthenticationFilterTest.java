package com.sky.movieratingservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomUserDetailService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String jwtToken = "valid.jwt.token";
    private final UUID userId = UUID.randomUUID();
    private final UserDetails userDetails = mock(UserDetails.class);

    @BeforeEach
    void setUp() {
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
        when(userDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void shouldSetAuthenticationWhenTokenIsValid() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(tokenProvider.validateToken(jwtToken)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(jwtToken)).thenReturn(userId);
        when(userDetailsService.loadUserById(userId)).thenReturn(userDetails);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        verify(tokenProvider).validateToken(jwtToken);
        verify(tokenProvider).getUserIdFromToken(jwtToken);
        verify(userDetailsService).loadUserById(userId);
        verify(filterChain).doFilter(request, response);

        // Clean up security context for other tests
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotSetAuthenticationWhenTokenIsMissing() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(request).getHeader("Authorization");
        verifyNoInteractions(tokenProvider, userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotSetAuthenticationWhenTokenIsInvalid() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(tokenProvider.validateToken(jwtToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(request).getHeader("Authorization");
        verify(tokenProvider).validateToken(jwtToken);
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldHandleExceptionGracefully() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwtToken);
        when(tokenProvider.validateToken(jwtToken)).thenThrow(new RuntimeException("Unexpected error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(request).getHeader("Authorization");
        verify(tokenProvider).validateToken(jwtToken);
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }
}
