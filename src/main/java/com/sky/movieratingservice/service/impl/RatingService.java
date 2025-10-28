package com.sky.movieratingservice.service.impl;

import com.sky.movieratingservice.api.dto.request.RatingRequestDto;
import com.sky.movieratingservice.api.dto.response.RatingResponseDto;
import com.sky.movieratingservice.domain.entity.Rating;
import com.sky.movieratingservice.domain.exception.ForbiddenException;
import com.sky.movieratingservice.domain.exception.ResourceNotFoundException;
import com.sky.movieratingservice.domain.repository.MovieRepository;
import com.sky.movieratingservice.domain.repository.RatingRepository;
import com.sky.movieratingservice.domain.repository.UserRepository;
import com.sky.movieratingservice.mapper.RatingMapper;
import com.sky.movieratingservice.service.IRatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService implements IRatingService {
    private final RatingRepository ratingRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final RatingMapper ratingMapper;

    @Override
    @Transactional
    public RatingResponseDto createOrUpdateRating(RatingRequestDto ratingRequestDto, UUID userID) {
        log.info("Create or update rating {}", ratingRequestDto);
        var user = userRepository.findById(userID).orElseThrow(() -> new ResourceNotFoundException("User", "userId", userID));

        var movie = movieRepository.findById(ratingRequestDto.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", ratingRequestDto.getMovieId()));

        Optional<Rating> existingRating = ratingRepository.findByUserIdAndMovieId(userID, movie.getId());
        Rating rating;
        if (existingRating.isPresent()) {
            //Update existing rating
            rating = existingRating.get();
            rating.setRatingValue(ratingRequestDto.getRatingValue());
            rating.setReview(ratingRequestDto.getReview());

            log.info("Updating Existing rating with ID {}", rating.getId());

        } else {
            // Create new rating
            rating = Rating.builder()
                    .ratingValue(ratingRequestDto.getRatingValue())
                    .user(user)
                    .movie(movie)
                    .review(ratingRequestDto.getReview())
                    .build();
            log.info("Creating new rating");
        }
        rating = ratingRepository.save(rating);
        return ratingMapper.toRatingResponse(rating);
    }

    @Override
    @Transactional
    public void deleteRating(UUID ratingId, UUID userId) {

        Rating rating = ratingRepository.findById(ratingId).orElseThrow(() ->
                new ResourceNotFoundException("Rating", "ratingId", ratingId));

        if(!rating.getUser().getId().equals(userId)){
            throw new ForbiddenException("You can only delete your own ratings");
        }
        ratingRepository.delete(rating);
        log.info("Deleting rating with ID {}", ratingId);

    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponseDto> getMovieRatings(UUID movieId) {
        log.info("Get all movie ratings for Movie {}", movieId);

        //Check if the movie exists
        movieRepository.findById(movieId).orElseThrow(() -> new ResourceNotFoundException("Movie", "movieId", movieId));

        var ratings = ratingRepository.findByMovieId(movieId);
        return ratings.stream()
                .map(ratingMapper::toRatingResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponseDto> getUserRatings(UUID userId) {
        log.info("Get all movie ratings for User {}", userId);
        var ratings = ratingRepository.findByUserIdWithDetails(userId);
        return ratings.stream()
                .map(ratingMapper::toRatingResponse)
                .toList();
    }

    @Override
    @Transactional
    public Optional<RatingResponseDto> getUserRatingForMovie(UUID movieId, UUID userId) {
        log.info("Get user rating for Movie {} and User {}", movieId, userId);
        return ratingRepository.findByUserIdAndMovieId(userId,movieId)
                .map(ratingMapper::toRatingResponse);
    }
}
