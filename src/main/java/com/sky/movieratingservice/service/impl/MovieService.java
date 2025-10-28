package com.sky.movieratingservice.service.impl;

import com.sky.movieratingservice.api.dto.request.CreateMovieRequestDto;
import com.sky.movieratingservice.api.dto.response.MovieDetailResponseDto;
import com.sky.movieratingservice.api.dto.response.MovieResponseDto;
import com.sky.movieratingservice.api.dto.response.TopRatedMovieResponseDto;
import com.sky.movieratingservice.domain.entity.Movie;
import com.sky.movieratingservice.domain.exception.ResourceNotFoundException;
import com.sky.movieratingservice.domain.repository.MovieRepository;
import com.sky.movieratingservice.domain.repository.RatingRepository;
import com.sky.movieratingservice.mapper.MovieMapper;
import com.sky.movieratingservice.service.IMovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MovieService implements IMovieService {
    private final Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final MovieRepository movieRepository;
    private final RatingRepository ratingRepository;
    private final MovieMapper movieMapper;

    @Override
    @Transactional(readOnly = true)
    public MovieDetailResponseDto getMovieById(UUID movieId) {
        logger.debug("Fetching movie details for ID: {}", movieId);

        var movie = movieRepository.findById(movieId).orElseThrow(() ->
                new ResourceNotFoundException("Movie", "id", movieId));

        MovieDetailResponseDto movieDetailResponseDto = movieMapper.toMovieDetailResponse(movie);

        var movieAvgRating = ratingRepository.findAverageRatingByMovieId(movieId).orElse(0.0);
        var movieRatingCount = ratingRepository.countByMovieId(movieId);

        movieDetailResponseDto.setAvgRating(movieAvgRating);
        movieDetailResponseDto.setRatingCount(movieRatingCount);
        return movieDetailResponseDto;

    }

    @Override
    @Transactional
    public MovieResponseDto createMovie(CreateMovieRequestDto movieRequestDto) {
        logger.debug("Creating new movie: {}", movieRequestDto);

        Movie movie = movieMapper.toMovie(movieRequestDto);
        movie = movieRepository.save(movie);

        logger.debug("Movie created: {}", movie);
        return movieMapper.toMovieResponse(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public TopRatedMovieResponseDto getTopRatedMovies() {
        logger.debug("Fetching top rated movies");


        Pageable pageable = PageRequest.of(0, 1);
        Page<MovieRepository.MovieStatistics> topRatedMovies = movieRepository.findTopRatedMovies(1L, pageable);

        if (topRatedMovies.isEmpty()) {
            throw new ResourceNotFoundException("N0 top rated movies found, no movies have ratings yet.");
        }

        return movieMapper.toTopRatedMoviesResponse(topRatedMovies.getContent().getFirst());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponseDto> getAllMovies(int page, int size) {
        logger.debug("Fetching all movies - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Movie> allMovies = movieRepository.findAll(pageable);

        return allMovies.getContent()
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }
}
