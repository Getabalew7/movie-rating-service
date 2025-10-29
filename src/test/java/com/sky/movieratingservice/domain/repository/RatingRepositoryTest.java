package com.sky.movieratingservice.domain.repository;

import com.sky.movieratingservice.domain.entity.Movie;
import com.sky.movieratingservice.domain.entity.Rating;
import com.sky.movieratingservice.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class RatingRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("movierating_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.contexts", () -> "test");
    }

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MovieRepository movieRepository;

    @Autowired
    protected RatingRepository ratingRepository;

    @Test
    void shouldSaveRating() {
        // Given
        User user = User.builder()
                .email("ratingtest@example.com")
                .password("password")
                .build();
        user = userRepository.save(user);

        Movie movie = Movie.builder()
                .name("Test Movie")
                .description("Test Description")
                .director("Test Director")
                .genre("Drama")
                .releaseYear(1900)
                .build();
        movie = movieRepository.save(movie);

        Rating rating = Rating.builder()
                .user(user)
                .movie(movie)
                .ratingValue(9)
                .review("Excellent!")
                .build();

        // When
        Rating savedRating = ratingRepository.save(rating);

        // Then
        assertThat(savedRating.getId()).isNotNull();
        assertThat(savedRating.getRatingValue()).isEqualTo(9);
        assertThat(savedRating.getReview()).isEqualTo("Excellent!");
    }

    @Test
    void shouldFindRatingByUserIdAndMovieId() {
        // Given
        User user = User.builder()
                .email("findtest@example.com")
                .password("password")
                .build();
        user = userRepository.save(user);

        Movie movie = Movie.builder()
                .name("Find Movie")
                .description("Test Description")
                .director("Test Director")
                .genre("Drama")
                .releaseYear(1900)
                .build();
        movie = movieRepository.save(movie);

        Rating rating = Rating.builder()
                .user(user)
                .movie(movie)
                .ratingValue(8)
                .build();
        ratingRepository.save(rating);

        // When
        Optional<Rating> found = ratingRepository.findByUserIdAndMovieId(
                user.getId(),
                movie.getId()
        );

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getRatingValue()).isEqualTo(8);
    }

    @Test
    void shouldCalculateAverageRating() {
        // Given
        Movie movie = Movie.builder()
                .name("Average Test Movie")
                .description("Test Description")
                .director("Test Director")
                .genre("Drama")
                .releaseYear(1900)
                .build();
        movie = movieRepository.save(movie);

        User user1 = userRepository.save(User.builder()
                .email("user1@example.com")
                .password("pass")
                .build());
        User user2 = userRepository.save(User.builder()
                .email("user2@example.com")
                .password("pass")
                .build());
        User user3 = userRepository.save(User.builder()
                .email("user3@example.com")
                .password("pass")
                .build());

        ratingRepository.save(Rating.builder()
                .user(user1)
                .movie(movie)
                .ratingValue(10)
                .build());
        ratingRepository.save(Rating.builder()
                .user(user2)
                .movie(movie)
                .ratingValue(8)
                .build());
        ratingRepository.save(Rating.builder()
                .user(user3)
                .movie(movie)
                .ratingValue(9)
                .build());

        // When
        Optional<Double> avgRating = ratingRepository.findAverageRatingByMovieId(movie.getId());

        // Then
        assertThat(avgRating).isPresent();
        assertThat(avgRating.get()).isEqualTo(9.0);
    }

    @Test
    void shouldCountRatingsByMovieId() {
        // Given
        Movie movie = Movie.builder()
                .name("Count Test Movie")
                .description("Test Description")
                .director("Test Director")
                .genre("Drama")
                .releaseYear(1900)
                .build();
        movie = movieRepository.save(movie);

        User user1 = userRepository.save(User.builder()
                .email("count1@example.com")
                .password("pass")
                .build());
        User user2 = userRepository.save(User.builder()
                .email("count2@example.com")
                .password("pass")
                .build());

        ratingRepository.save(Rating.builder()
                .user(user1)
                .movie(movie)
                .ratingValue(8)
                .build());
        ratingRepository.save(Rating.builder()
                .user(user2)
                .movie(movie)
                .ratingValue(9)
                .build());

        // When
        long count = ratingRepository.countByMovieId(movie.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldEnforceUniqueConstraintOnUserAndMovie() {
        // Given
        User user = userRepository.save(User.builder()
                .email("unique@example.com")
                .password("pass")
                .build());

        Movie movie = movieRepository.save(Movie.builder()
                .name("Unique Movie")
                .build());

        ratingRepository.save(Rating.builder()
                .user(user)
                .movie(movie)
                .ratingValue(8)
                .build());

        // When & Then: Try to save duplicate
        Rating duplicate = Rating.builder()
                .user(user)
                .movie(movie)
                .ratingValue(9)
                .build();

        assertThatThrownBy(() -> {
            ratingRepository.save(duplicate);
            ratingRepository.flush(); // Force constraint check
        }).isInstanceOf(Exception.class); // DataIntegrityViolationException
    }

    @Test
    void shouldFindRatingsByUserIdWithDetails() {
        // Given
        User user = userRepository.save(User.builder()
                .email("details@example.com")
                .password("pass")
                .build());

        Movie movie1 = movieRepository.save(Movie.builder()
                .name("Movie 1")
                .description("Test Description")
                .director("Test Director")
                .genre("Drama")
                .releaseYear(1900)
                .build());
        Movie movie2 = movieRepository.save(Movie.builder()
                .name("Movie 2")
                .description("Test Description")
                .director("Test Director")
                .genre("Drama")
                .releaseYear(1900)
                .build());

        ratingRepository.save(Rating.builder()
                .user(user)
                .movie(movie1)
                .ratingValue(8)
                .build());
        ratingRepository.save(Rating.builder()
                .user(user)
                .movie(movie2)
                .ratingValue(9)
                .build());

        // When
        var ratings = ratingRepository.findByUserIdWithDetails(user.getId());

        // Then
        assertEquals(2, ratings.size());
        assertThat(ratings.get(0).getMovie()).isNotNull();
        assertThat(ratings.get(0).getUser()).isNotNull();
    }
}