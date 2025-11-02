package com.sky.movieratingservice.api.controller;

import com.sky.movieratingservice.api.dto.request.RatingRequestDto;
import com.sky.movieratingservice.common.AbstractIntegrationTest;
import com.sky.movieratingservice.domain.entity.Movie;
import com.sky.movieratingservice.domain.entity.Rating;
import com.sky.movieratingservice.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

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
        webClient.post()
                .uri("/api/v1/ratings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.ratingValue").isEqualTo(9)
                .jsonPath("$.review").isEqualTo("Excellent movie!")
                .jsonPath("$.movieId").isEqualTo(movie.getId().toString());
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
        webClient.post()
                .uri("/api/v1/ratings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.ratingValue").isEqualTo(8)
                .jsonPath("$.review").isEqualTo("Actually, it's great!");
    }

    @Test
    void shouldFailCreateRatingWithoutAuthentication() {
        Movie movie = movieRepository.findAll().getFirst();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(movie.getId())
                .ratingValue(9)
                .build();

        webClient.post()
                .uri("/api/v1/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldFailCreateRatingWithInvalidValue() throws Exception {
        String token = registerAndGetToken("validator@example.com");
        Movie movie = movieRepository.findAll().getFirst();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(movie.getId())
                .ratingValue(11) // Invalid: must be 1-10
                .build();

        webClient.post()
                .uri("/api/v1/ratings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.validationErrors").exists();
    }

    @Test
    void shouldFailCreateRatingForNonExistentMovie() throws Exception {
        String token = registerAndGetToken("nonexist@example.com");
        UUID nonExistentMovieId = UUID.randomUUID();

        RatingRequestDto request = RatingRequestDto.builder()
                .movieId(nonExistentMovieId)
                .ratingValue(8)
                .build();

        webClient.post()
                .uri("/api/v1/ratings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").value(msg -> {
                    assert msg.toString().contains("not found");
                });
    }

    @Test
    void shouldGetMyRatings() throws Exception {
        String token = registerAndGetToken("myratings@example.com");
        User user = userRepository.findByEmail("myratings@example.com").orElseThrow();
        Movie movie1 = movieRepository.findAll().get(0);
        Movie movie2 = movieRepository.findAll().get(1);

        Rating rating1 = Rating.builder().user(user).movie(movie1).ratingValue(9).build();
        Rating rating2 = Rating.builder().user(user).movie(movie2).ratingValue(7).build();
        ratingRepository.save(rating1);
        ratingRepository.save(rating2);

        webClient.get()
                .uri("/api/v1/ratings/my")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$").value(list -> {
                    assert ((List<?>) list).size() == 2;
                });
    }

    @Test
    void shouldDeleteOwnRating() throws Exception {
        String token = registerAndGetToken("deleter@example.com");
        User user = userRepository.findByEmail("deleter@example.com").orElseThrow();
        Movie movie = movieRepository.findAll().getFirst();

        Rating rating = Rating.builder()
                .user(user)
                .movie(movie)
                .ratingValue(8)
                .build();
        rating = ratingRepository.save(rating);

        webClient.delete()
                .uri("/api/v1/ratings/{ratingId}", rating.getId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        // Verify deletion
        webClient.get()
                .uri("/api/v1/ratings/my")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").value(list -> {
                    assert ((List<?>) list).isEmpty();
                });
    }

    @Test
    void shouldFailDeleteOtherUserRating() throws Exception {
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

        webClient.delete()
                .uri("/api/v1/ratings/{ratingId}", rating.getId())
                .header("Authorization", "Bearer " + token2)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.message").value(msg -> {
                    assert msg.toString().contains("your own ratings");
                });
    }

    @Test
    void shouldGetMyRatingForSpecificMovie() throws Exception {
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

        webClient.get()
                .uri("/api/v1/ratings/my/movie/{movieId}", movie.getId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ratingValue").isEqualTo(9)
                .jsonPath("$.review").isEqualTo("Great!");
    }

    @Test
    void shouldReturn404WhenNoRatingForMovie() throws Exception {
        String token = registerAndGetToken("norating@example.com");
        Movie movie = movieRepository.findAll().getFirst();

        webClient.get()
                .uri("/api/v1/ratings/my/movie/{movieId}", movie.getId())
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound();
    }

    // Helper method
    protected String registerAndGetToken(String email) throws Exception {
        return registerAndGetToken(email, "Pass123!@");
    }


}