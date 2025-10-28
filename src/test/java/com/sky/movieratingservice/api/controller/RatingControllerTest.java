package com.sky.movieratingservice.api.controller;

import com.sky.movieratingservice.api.dto.request.RatingRequestDto;
import com.sky.movieratingservice.domain.entity.Movie;
import com.sky.movieratingservice.domain.entity.Rating;
import com.sky.movieratingservice.domain.entity.User;
import com.sky.movieratingservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RatingControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateRatingSuccessfully() throws Exception {
        // Given: User is authenticated
        String token = registerAndGetToken("rater@example.com");
        Movie movie = movieRepository.findAll().getFirst();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(movie.getId())
                .ratingValue(9)
                .review("Excellent movie!")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/ratings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ratingValue").value(9))
                .andExpect(jsonPath("$.review").value("Excellent movie!"))
                .andExpect(jsonPath("$.movieId").value(movie.getId().toString()));
    }

    @Test
    void shouldUpdateExistingRating() throws Exception {
        // Given: User has already rated the movie
        String token = registerAndGetToken("updater@example.com");
        User user = userRepository.findByEmail("updater@example.com").orElseThrow();
        Movie movie = movieRepository.findAll().getFirst();

        Rating existingRating = Rating.builder()
                .user(user)
                .movie(movie)
                .ratingValue(5)
                .review("It was okay")
                .build();
        ratingRepository.save(existingRating);

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(movie.getId())
                .ratingValue(8)
                .review("Actually, it's great!")
                .build();

        // When & Then: Update should work
        mockMvc.perform(post("/api/v1/ratings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ratingValue").value(8))
                .andExpect(jsonPath("$.review").value("Actually, it's great!"));
    }

    @Test
    void shouldFailCreateRatingWithoutAuthentication() throws Exception {
        Movie movie = movieRepository.findAll().getFirst();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(movie.getId())
                .ratingValue(9)
                .build();

        mockMvc.perform(post("/api/v1/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailCreateRatingWithInvalidValue() throws Exception {
        String token = registerAndGetToken("validator@example.com");
        Movie movie = movieRepository.findAll().getFirst();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(movie.getId())
                .ratingValue(11) // Invalid: must be 1-10
                .build();

        mockMvc.perform(post("/api/v1/ratings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    void shouldFailCreateRatingForNonExistentMovie() throws Exception {
        String token = registerAndGetToken("nonexist@example.com");
        UUID nonExistentMovieId = UUID.randomUUID();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(nonExistentMovieId)
                .ratingValue(8)
                .build();

        mockMvc.perform(post("/api/v1/ratings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void shouldGetMyRatings() throws Exception {
        // Given: User has ratings
        String token = registerAndGetToken("myratings@example.com");
        User user = userRepository.findByEmail("myratings@example.com").orElseThrow();
        Movie movie1 = movieRepository.findAll().get(0);
        Movie movie2 = movieRepository.findAll().get(1);

        Rating rating1 = Rating.builder()
                .user(user)
                .movie(movie1)
                .ratingValue(9)
                .build();
        Rating rating2 = Rating.builder()
                .user(user)
                .movie(movie2)
                .ratingValue(7)
                .build();
        ratingRepository.save(rating1);
        ratingRepository.save(rating2);

        // When & Then
        mockMvc.perform(get("/api/v1/ratings/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldDeleteOwnRating() throws Exception {
        // Given: User has a rating
        String token = registerAndGetToken("deleter@example.com");
        User user = userRepository.findByEmail("deleter@example.com").orElseThrow();
        Movie movie = movieRepository.findAll().getFirst();

        Rating rating = Rating.builder()
                .user(user)
                .movie(movie)
                .ratingValue(8)
                .build();
        rating = ratingRepository.save(rating);

        // When & Then
        mockMvc.perform(delete("/api/v1/ratings/{ratingId}", rating.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/v1/ratings/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldFailDeleteOtherUserRating() throws Exception {
        // Given: Two users, one rating
        registerAndGetToken("user1@example.com");
        String token2 = registerAndGetToken("user2@example.com");

        User user1 = userRepository.findByEmail("user1@example.com").orElseThrow();
        Movie movie = movieRepository.findAll().getFirst();

        Rating rating = Rating.builder()
                .user(user1)
                .movie(movie)
                .ratingValue(8)
                .build();
        rating = ratingRepository.save(rating);

        // When & Then: User2 tries to delete User1's rating
        mockMvc.perform(delete("/api/v1/ratings/{ratingId}", rating.getId())
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("your own ratings")));
    }

    @Test
    void shouldGetMyRatingForSpecificMovie() throws Exception {
        // Given: User has rated a movie
        String token = registerAndGetToken("specific@example.com");
        User user = userRepository.findByEmail("specific@example.com").orElseThrow();
        Movie movie = movieRepository.findAll().getFirst();

        Rating rating = Rating.builder()
                .user(user)
                .movie(movie)
                .ratingValue(9)
                .review("Great!")
                .build();
        ratingRepository.save(rating);

        // When & Then
        mockMvc.perform(get("/api/v1/ratings/my/movie/{movieId}", movie.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratingValue").value(9))
                .andExpect(jsonPath("$.review").value("Great!"));
    }

    @Test
    void shouldReturn404WhenNoRatingForMovie() throws Exception {
        String token = registerAndGetToken("norating@example.com");
        Movie movie = movieRepository.findAll().getFirst();

        mockMvc.perform(get("/api/v1/ratings/my/movie/{movieId}", movie.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // Helper method
    protected String registerAndGetToken(String email) throws Exception {
        return registerAndGetToken(email, "Pass123!@");
    }

}