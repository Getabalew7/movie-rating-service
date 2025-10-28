package com.sky.movieratingservice.api.controller;

import com.sky.movieratingservice.api.dto.request.RatingRequestDto;
import com.sky.movieratingservice.domain.entity.Movie;
import com.sky.movieratingservice.domain.entity.Rating;
import com.sky.movieratingservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class MovieControllerTest extends AbstractIntegrationTest {

    @Test
    void ShouldGetAllMoviesWithoutAuthentication() throws Exception {

        mockMvc.perform(get("/api/v1/movies")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
        ;
    }

    @Test
    void shouldGetMovieByIdWithStatistics() throws Exception {
        // Given: Movie exists
        Movie movie = movieRepository.findAll().getFirst();

        // When & Then
        mockMvc.perform(get("/api/v1/movies/{movieId}", movie.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(movie.getId().toString()))
                .andExpect(jsonPath("$.name").value(movie.getName()))
                .andExpect(jsonPath("$.avgRating").isNumber())
                .andExpect(jsonPath("$.ratingCount").isNumber());
    }

    @Test
    void shouldFailGetMovieByIdWhenNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/movies/{movieId}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    @Test
    void shouldGetTopRatedMovie() throws Exception {
        // Given: Movies with ratings exist (from seed data or created in test)
        Movie movieFirst = movieRepository.findAll().getFirst();

        Movie movieSecond = movieRepository.findAll().getLast();

        //Given a user from seed data

        registerAndGetToken("movie@movie.com", "Password123!");

        //create a movie rating

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
        mockMvc.perform(get("/api/v1/movies/top-rated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.avgRating").isNumber())
                .andExpect(jsonPath("$.avgRating").value(ratingFirstMovie.getRatingValue()))
                .andExpect(jsonPath("$.ratingCount").isNumber())
                .andExpect(jsonPath("$.ratingCount").value(1));
    }

    @Test
    void shouldValidatePaginationParameters() throws Exception {
        mockMvc.perform(get("/api/v1/movies")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLimitPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/movies")
                        .param("page", "0")
                        .param("size", "150")) // Exceeds max of 100
                .andExpect(status().isBadRequest());
    }


}