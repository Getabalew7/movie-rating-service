package com.sky.movieratingservice.mapper;

import com.sky.movieratingservice.api.dto.request.CreateMovieRequestDto;
import com.sky.movieratingservice.api.dto.response.MovieDetailResponseDto;
import com.sky.movieratingservice.api.dto.response.MovieResponseDto;
import com.sky.movieratingservice.api.dto.response.TopRatedMovieResponseDto;
import com.sky.movieratingservice.domain.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    MovieResponseDto toMovieResponse(Movie movie);

    Movie toMovie(CreateMovieRequestDto createMovieRequestDto);

    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    MovieDetailResponseDto toMovieDetailResponse(Movie movie);

    @Mapping(target = "avgRating", source = "avgRating")
    @Mapping(source = "countMovieRating",target  ="ratingCount")
    @Mapping(source = "movie.id", target = "id")
    @Mapping(source = "movie.name", target = "name")
    @Mapping(source = "movie.description", target = "description")
    @Mapping(source = "movie.releaseYear", target = "releaseYear")
    @Mapping(source = "movie.genre", target = "genre")
    @Mapping(source = "movie.director", target = "director")
    TopRatedMovieResponseDto toTopRatedMoviesResponse(Movie movie, Double avgRating, Long countMovieRating);
}
