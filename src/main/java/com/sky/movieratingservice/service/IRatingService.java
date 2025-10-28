package com.sky.movieratingservice.service;

import com.sky.movieratingservice.api.dto.request.RatingRequestDto;
import com.sky.movieratingservice.api.dto.response.RatingResponseDto;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IRatingService {
    RatingResponseDto createOrUpdateRating(@Valid RatingRequestDto ratingRequestDto, UUID userID);

    void deleteRating(UUID ratingId, UUID userId);

    List<RatingResponseDto> getMovieRatings(UUID movieId);

    List<RatingResponseDto>  getUserRatings(UUID userId);

    Optional<RatingResponseDto> getUserRatingForMovie(UUID movieId, UUID userId);
}
