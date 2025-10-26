package com.sky.movieratingservice.mapper;

import com.sky.movieratingservice.api.dto.request.CreateMovieRequestDto;
import com.sky.movieratingservice.api.dto.response.MovieDetailResponseDto;
import com.sky.movieratingservice.api.dto.response.MovieResponseDto;
import com.sky.movieratingservice.domain.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MovieMapper {

    MovieResponseDto toMovieResponse(Movie movie);

    Movie toMovie(CreateMovieRequestDto createMovieRequestDto);

    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    MovieDetailResponseDto toMovieDetailResponse(Movie movie);
}
