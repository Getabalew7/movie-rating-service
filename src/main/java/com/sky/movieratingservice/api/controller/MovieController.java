package com.sky.movieratingservice.api.controller;

import com.sky.movieratingservice.api.dto.request.CreateMovieRequestDto;
import com.sky.movieratingservice.api.dto.response.MovieDetailResponseDto;
import com.sky.movieratingservice.api.dto.response.MovieResponseDto;
import com.sky.movieratingservice.api.dto.response.TopRatedMovieResponseDto;
import com.sky.movieratingservice.domain.entity.Movie;
import com.sky.movieratingservice.service.IMovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/movies", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Tag(name = "Movie Controller", description = "APIs for managing movies")
public class MovieController {

    private final IMovieService movieService;

    @GetMapping
    @Operation(
            summary = "Get All Movies",
            description = "Retrieve a paginated list of all movies. No authentication required."
    )
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved list of movies"
                    )
            }
    )
    public ResponseEntity<List<MovieResponseDto>> getAllMovies(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        List<MovieResponseDto> movies =  movieService.getAllMovies(page, size);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{movieId}")
    @Operation(
            summary = "Get Movie by ID",
            description = "Retrieve detailed information about a specific movie by its ID. No authentication required."
    )
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved movie details",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = MovieDetailResponseDto.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Movie not found"
                    )
            }
    )
    public ResponseEntity<MovieDetailResponseDto> getMovie(@PathVariable UUID movieId){

        MovieDetailResponseDto movieDetail = movieService.getMovieById(movieId);
        return ResponseEntity.ok(movieDetail);
    }


@GetMapping("/top-rated")
    @Operation(
            summary = "Get Top Rated Movies",
            description = "Retrieve a list of top-rated movies. No authentication required."
    )
@ApiResponses(
        value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Successfully retrieved top-rated movies",
                        content = @io.swagger.v3.oas.annotations.media.Content(
                                mediaType = "application/json",
                                schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TopRatedMovieResponseDto.class)
                        )
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Top rated movies not found"
                )
        }
)
    public ResponseEntity<TopRatedMovieResponseDto> getTopRatedMovies(){
        TopRatedMovieResponseDto topRatedMovieResponseDto = movieService.getTopRatedMovies();
        return ResponseEntity.ok(topRatedMovieResponseDto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Create a New Movie",
            description = "Create a new movie entry. Requires ADMIN role."
    )
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Successfully created a new movie",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = MovieResponseDto.class)
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - You don't have permission to create a movie"
                    )
            }
    )
    public ResponseEntity<MovieResponseDto> createMovie(@Validated @RequestBody CreateMovieRequestDto movieRequestDto){
        var movie = movieService.createMovie(movieRequestDto);
        return ResponseEntity.ok(movie);
    }
}
