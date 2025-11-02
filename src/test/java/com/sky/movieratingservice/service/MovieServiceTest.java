package com.sky.movieratingservice.service;

import com.sky.movieratingservice.api.dto.request.CreateMovieRequestDto;
import com.sky.movieratingservice.api.dto.response.MovieDetailResponseDto;
import com.sky.movieratingservice.api.dto.response.MovieResponseDto;
import com.sky.movieratingservice.api.dto.response.TopRatedMovieResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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
    @Test
    void shouldCreateMovieSuccessfully() {
        // Given
        CreateMovieRequestDto requestDto = CreateMovieRequestDto.builder()
                .name("New Movie")
                .build();

        Movie movie = Movie.builder()
                .id(UUID.randomUUID())
                .name("New Movie")
                .build();

        MovieResponseDto responseDto = MovieResponseDto.builder()
                .id(movie.getId())
                .name(movie.getName())
                .build();

        when(movieMapper.toMovie(requestDto)).thenReturn(movie);
        when(movieRepository.save(movie)).thenReturn(movie);
        when(movieMapper.toMovieResponse(movie)).thenReturn(responseDto);

        // When
        MovieResponseDto result = movieService.createMovie(requestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(movie.getId());
        assertThat(result.getName()).isEqualTo("New Movie");

        verify(movieMapper).toMovie(requestDto);
        verify(movieRepository).save(movie);
        verify(movieMapper).toMovieResponse(movie);
    }

    @Test
    void shouldGetTopRatedMoviesSuccessfully() {
        // Given
        MovieRepository.MovieStatistics stats = mock(MovieRepository.MovieStatistics.class);

        UUID movieId = UUID.randomUUID();
//        when(stats.getMovieId()).thenReturn(movieId);
//        when(stats.getMovieName()).thenReturn("Top Movie");
//        when(stats.getAvgRating()).thenReturn(5.0);
//        when(stats.getRatingCount()).thenReturn(100L);

        Page<MovieRepository.MovieStatistics> page = new PageImpl<>(List.of(stats));

        when(movieRepository.findTopRatedMovies(anyLong(), any(Pageable.class)))
                .thenReturn(page);
        when(movieMapper.toTopRatedMoviesResponse(stats)).thenReturn(
                TopRatedMovieResponseDto.builder()
                        .id(movieId)
                        .name("Top Movie")
                        .avgRating(5.0)
                        .ratingCount(100L)
                        .build()
        );

        // When
        TopRatedMovieResponseDto result = movieService.getTopRatedMovies();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Top Movie");

        verify(movieRepository).findTopRatedMovies(anyLong(), any(Pageable.class));
        verify(movieMapper).toTopRatedMoviesResponse(stats);
    }


    @Test
    void shouldThrowExceptionWhenNoTopRatedMovies() {
        // Given
        Page<MovieRepository.MovieStatistics> emptyPage = Page.empty();
        when(movieRepository.findTopRatedMovies(anyLong(), any(Pageable.class))).thenReturn(emptyPage);

        // When & Then
        assertThatThrownBy(() -> movieService.getTopRatedMovies())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no movies have ratings yet");

        verify(movieRepository).findTopRatedMovies(anyLong(), any(Pageable.class));
    }

    @Test
    void shouldGetAllMoviesSuccessfully() {
        // Given
        Movie movie1 = Movie.builder().id(UUID.randomUUID()).name("Movie A").build();
        Movie movie2 = Movie.builder().id(UUID.randomUUID()).name("Movie B").build();

        Page<Movie> page = new PageImpl<>(List.of(movie1, movie2));

        when(movieRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(movieMapper.toMovieResponse(movie1)).thenReturn(MovieResponseDto.builder()
                .id(movie1.getId()).name(movie1.getName()).build());
        when(movieMapper.toMovieResponse(movie2)).thenReturn(MovieResponseDto.builder()
                .id(movie2.getId()).name(movie2.getName()).build());

        // When
        List<MovieResponseDto> result = movieService.getAllMovies(0, 10);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Movie A");
        assertThat(result.get(1).getName()).isEqualTo("Movie B");

        verify(movieRepository).findAll(any(Pageable.class));
        verify(movieMapper).toMovieResponse(movie1);
        verify(movieMapper).toMovieResponse(movie2);
    }


}