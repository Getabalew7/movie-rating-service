package com.sky.movieratingservice.mapper;

import com.sky.movieratingservice.api.dto.response.RatingResponseDto;
import com.sky.movieratingservice.domain.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RatingMapper {

    @Mapping(source = "rating.user.id", target = "userId")
    @Mapping(source = "rating.movie.name", target = "movieName")
    @Mapping(source = "rating.movie.id", target = "movieId")
    @Mapping(source = "rating.user.email", target = "userEmail")
    RatingResponseDto toRatingResponse(Rating rating);
}
