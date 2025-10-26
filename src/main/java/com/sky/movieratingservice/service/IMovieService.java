package com.sky.movieratingservice.service;

import com.sky.movieratingservice.api.dto.request.CreateMovieRequestDto;
import com.sky.movieratingservice.api.dto.response.MovieDetailResponseDto;
import com.sky.movieratingservice.api.dto.response.MovieResponseDto;
import com.sky.movieratingservice.api.dto.response.TopRatedMovieResponseDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;
import java.util.UUID;

public interface IMovieService {
    List<MovieResponseDto> getAllMovies(@Min(0) int page, @Min(1) @Max(100) int size);

    MovieDetailResponseDto getMovieById(UUID movieId);

    TopRatedMovieResponseDto getTopRatedMovies();

    MovieResponseDto createMovie(CreateMovieRequestDto movieRequestDto);
}
