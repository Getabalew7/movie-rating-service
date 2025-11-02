package com.sky.movieratingservice.api.controller;

import com.sky.movieratingservice.api.dto.request.CreateMovieRequestDto;
import com.sky.movieratingservice.common.AbstractIntegrationTest;
import com.sky.movieratingservice.domain.entity.Movie;
import com.sky.movieratingservice.domain.entity.Rating;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

class MovieControllerTest extends AbstractIntegrationTest {

    @Test
    void ShouldGetAllMoviesWithoutAuthentication() {
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/movies")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].id").exists()
                .jsonPath("$[0].name").exists();
    }

    @Test
    void shouldGetMovieByIdWithStatistics() {
        // Given: Movie exists
        Movie movie = movieRepository.findAll().getFirst();

        webClient.get()
                .uri("/api/v1/movies/{movieId}", movie.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(movie.getId().toString())
                .jsonPath("$.name").isEqualTo(movie.getName())
                .jsonPath("$.avgRating").isNumber()
                .jsonPath("$.ratingCount").isNumber();
    }

    @Test
    void shouldFailGetMovieByIdWhenNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        webClient.get()
                .uri("/api/v1/movies/{movieId}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").value(msg -> {
                    assert msg.toString().contains("not found");
                });
    }

    @Test
    void shouldGetTopRatedMovie() throws Exception {
        // Given: Movies with ratings exist
        Movie movieFirst = movieRepository.findAll().getFirst();
        Movie movieSecond = movieRepository.findAll().getLast();

        // Register user
        String token = registerAndGetToken("movie@movie.com", "Password123!");

        // Create movie ratings
        Rating ratingFirstMovie = Rating.builder()
                .movie(movieFirst)
                .ratingValue(10)
                .review("Amazing movie!")
                .user(userRepository.findByEmail("movie@movie.com").orElse(null))
                .build();
        ratingRepository.save(ratingFirstMovie);

        Rating ratingSecondMovie = Rating.builder()
                .movie(movieSecond)
                .ratingValue(8)
                .review("Good movie!")
                .user(userRepository.findByEmail("movie@movie.com").orElse(null))
                .build();
        ratingRepository.save(ratingSecondMovie);

        // When & Then
        webClient.get()
                .uri("/api/v1/movies/top-rated")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").exists()
                .jsonPath("$.avgRating").isNumber()
                .jsonPath("$.avgRating").isEqualTo(ratingFirstMovie.getRatingValue())
                .jsonPath("$.ratingCount").isNumber()
                .jsonPath("$.ratingCount").isEqualTo(1);
    }

    @Test
    void shouldValidatePaginationParameters() {
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/movies")
                        .queryParam("page", "-1")
                        .queryParam("size", "0")
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldLimitPageSize() {
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/movies")
                        .queryParam("page", "0")
                        .queryParam("size", "150") // Exceeds max of 100
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldRequireAuthenticationForCreateMovieRating() throws Exception {
        CreateMovieRequestDto movieRequestDto = CreateMovieRequestDto.builder()
                .name("Test Movie")
                .description("Test Description")
                .director("Test Director")
                .genre("Drama")
                .releaseYear(1900)
                .build();

        // Attempt to create movie without authentication
        webClient.post()
                .uri("/api/v1/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movieRequestDto)
                .exchange()
                .expectStatus().isUnauthorized();

        // Create movie with authentication
        String token = registerAndGetToken("createmovie@movie.com", "Password@123!");
        webClient.post()
                .uri("/api/v1/movies")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(movieRequestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.name").isEqualTo(movieRequestDto.getName());
    }

}