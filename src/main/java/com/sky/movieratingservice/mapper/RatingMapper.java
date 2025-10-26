package com.sky.movieratingservice.mapper;

import com.sky.movieratingservice.api.dto.response.RatingResponseDto;
import com.sky.movieratingservice.domain.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RatingMapper {

    RatingResponseDto toRatingReponse(Rating rating);
}
