package com.sky.movieratingservice.service;

import com.sky.movieratingservice.api.dto.request.RatingRequestDto;
import com.sky.movieratingservice.domain.entity.Movie;
import com.sky.movieratingservice.domain.entity.Rating;
import com.sky.movieratingservice.domain.entity.User;
import com.sky.movieratingservice.domain.exception.ForbiddenException;
import com.sky.movieratingservice.domain.exception.ResourceNotFoundException;
import com.sky.movieratingservice.domain.repository.MovieRepository;
import com.sky.movieratingservice.domain.repository.RatingRepository;
import com.sky.movieratingservice.domain.repository.UserRepository;
import com.sky.movieratingservice.mapper.RatingMapper;
import com.sky.movieratingservice.service.impl.RatingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {
    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private RatingMapper ratingMapper;

    @InjectMocks
    private RatingService ratingService;

    @Test
    void shouldCreateNewRating() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        User user = User.builder().id(userId).email("test@example.com").build();
        Movie movie = Movie.builder().id(movieId).name("Test Movie").build();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(movieId)
                .ratingValue(9)
                .review("Great!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(ratingRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(Rating.class))).thenAnswer(i -> i.getArgument(0));

        // When
        ratingService.createOrUpdateRating( request,userId);

        // Then
        verify(ratingRepository).save(any(Rating.class));
    }
    @Test
    void shouldUpdateExistingRating() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        User user = User.builder().id(userId).email("test@example.com").build();
        Movie movie = Movie.builder().id(movieId).name("Test Movie").build();
        Rating existingRating = Rating.builder()
                .id(UUID.randomUUID())
                .user(user)
                .movie(movie)
                .ratingValue(5)
                .build();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(movieId)
                .ratingValue(9)
                .review("Changed my mind!")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
        when(ratingRepository.findByUserIdAndMovieId(userId, movieId)).thenReturn(Optional.of(existingRating));
        when(ratingRepository.save(any(Rating.class))).thenAnswer(i -> i.getArgument(0));

        // When
        ratingService.createOrUpdateRating(request,userId);

        // Then
        verify(ratingRepository).save(existingRating);
        assertThat(existingRating.getRatingValue()).isEqualTo(9);
        assertThat(existingRating.getReview()).isEqualTo("Changed my mind!");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(movieId)
                .ratingValue(9)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ratingService.createOrUpdateRating(request, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void shouldThrowExceptionWhenMovieNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        User user = User.builder().id(userId).email("test@example.com").build();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(movieId)
                .ratingValue(9)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ratingService.createOrUpdateRating(request, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Movie");
    }

    @Test
    void shouldDeleteOwnRating() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        User user = User.builder().id(userId).build();
        Rating rating = Rating.builder()
                .id(ratingId)
                .user(user)
                .build();

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(rating));

        // When
        ratingService.deleteRating(ratingId, userId);

        // Then
        verify(ratingRepository).delete(rating);
    }

    @Test
    void shouldThrowExceptionWhenDeletingOtherUserRating() {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        User user1 = User.builder().id(userId1).build();
        Rating rating = Rating.builder()
                .id(ratingId)
                .user(user1)
                .build();

        when(ratingRepository.findById(ratingId)).thenReturn(Optional.of(rating));

        // When & Then
        assertThatThrownBy(() -> ratingService.deleteRating(ratingId, userId2))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("your own ratings");

        verify(ratingRepository, never()).delete(any());
    }

}