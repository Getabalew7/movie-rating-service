package com.sky.movieratingservice.service;

import com.sky.movieratingservice.api.dto.response.MovieDetailResponseDto;
import com.sky.movieratingservice.domain.entity.Movie;
import com.sky.movieratingservice.domain.exception.ResourceNotFoundException;
import com.sky.movieratingservice.domain.repository.MovieRepository;
import com.sky.movieratingservice.domain.repository.RatingRepository;
import com.sky.movieratingservice.mapper.MovieMapper;
import com.sky.movieratingservice.service.impl.MovieService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    void shouldGetMovieByIdSuccessfully() {

        Movie movie = Movie.builder()
                .id(UUID.randomUUID())
                .name("Test movie")
                .build();

        when(movieRepository.findById(movie.getId())).thenReturn(Optional.of(movie));
        when(movieMapper.toMovieDetailResponse(movie)).thenReturn(MovieDetailResponseDto.builder()
                .id(movie.getId())
                .name(movie.getName())
                .build());
        when(ratingRepository.findAverageRatingByMovieId(movie.getId())).thenReturn(Optional.of(4.5));
        when(ratingRepository.countByMovieId(any())).thenReturn(10L);

        movieService.getMovieById(movie.getId());

        verify(movieRepository).findById(movie.getId());
        verify(ratingRepository).findAverageRatingByMovieId(movie.getId());
        verify(ratingRepository).countByMovieId(movie.getId());
    }
    @Test
    void shouldThrowExceptionWhenMovieNotFound() {
        // Given
        UUID movieId = UUID.randomUUID();
        when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> movieService.getMovieById(movieId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");

        verify(movieRepository).findById(movieId);
        verifyNoInteractions(ratingRepository);
    }

}