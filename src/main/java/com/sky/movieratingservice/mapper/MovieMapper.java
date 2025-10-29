package com.sky.movieratingservice.mapper;

import com.sky.movieratingservice.api.dto.request.CreateMovieRequestDto;
import com.sky.movieratingservice.api.dto.response.MovieDetailResponseDto;
import com.sky.movieratingservice.api.dto.response.MovieResponseDto;
import com.sky.movieratingservice.api.dto.response.TopRatedMovieResponseDto;
import com.sky.movieratingservice.domain.entity.Movie;
import com.sky.movieratingservice.domain.repository.MovieRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    @Mapping(target = "createdAt", ignore = true)
    MovieResponseDto toMovieResponse(Movie movie);

    Movie toMovie(CreateMovieRequestDto createMovieRequestDto);

    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    MovieDetailResponseDto toMovieDetailResponse(Movie movie);

    @Mapping(source = "movieId", target = "id")
    @Mapping(source = "movieName", target = "name")
    @Mapping(source = "movieDescription", target = "description")
    TopRatedMovieResponseDto toTopRatedMoviesResponse(MovieRepository.MovieStatistics first);
}
