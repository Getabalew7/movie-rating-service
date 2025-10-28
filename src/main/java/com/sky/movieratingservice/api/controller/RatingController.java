package com.sky.movieratingservice.api.controller;

import com.sky.movieratingservice.api.dto.request.RatingRequestDto;
import com.sky.movieratingservice.api.dto.response.RatingResponseDto;
import com.sky.movieratingservice.service.IRatingService;
import com.sky.movieratingservice.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/ratings", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
@Tag(name = "Rating Controller", description = "Endpoints for managing movie ratings")
@SecurityRequirement(name = "Bearer Authentication")
public class RatingController {
    private final IRatingService ratingService;


    @PostMapping
    @Operation(
            summary = "Create or update a movie rating",
            description = "Allows an authenticated user to create a new rating or update an existing rating for a movie."
    )
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "201",
                            description = "Rating created or updated successfully",
                            content = {
                                    @io.swagger.v3.oas.annotations.media.Content(
                                            mediaType = "application/json",
                                            schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = RatingResponseDto.class)
                                    )
                            }
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "Invalid input data"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - authentication required"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Movie not found"
                    )
            }
    )
    public ResponseEntity<RatingResponseDto> createOrUpdateRating(@Valid @RequestBody RatingRequestDto ratingRequestDto,
                                                                  @AuthenticationPrincipal UserPrincipal userPrincipal) {
        RatingResponseDto ratingResponseDto = ratingService.createOrUpdateRating(ratingRequestDto, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingResponseDto);
    }

    @DeleteMapping("/{ratingId}")
    @Operation(
            summary = "Delete a movie rating",
            description = "Allows an authenticated user to delete their rating for a movie."
    )
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "204",
                            description = "Rating deleted successfully"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - authentication required"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "Can't delete rating of another user rating"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "Rating not found"
                    )
            }
    )
    public ResponseEntity<Void> deleteRating(@Parameter(description = "ID of the rating to delete") @PathVariable UUID ratingId,
                                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ratingService.deleteRating(ratingId, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/my")
    @Operation(
            summary = "Get my ratings",
            description = "Get all ratings created by authenticated user"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ratings retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<List<RatingResponseDto>> getMyRatings(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<RatingResponseDto> ratings = ratingService.getUserRatings(currentUser.getId());

        return ResponseEntity.status(HttpStatus.OK).body(ratings);
    }
    @GetMapping("/my/movie/{movieId}")
    @Operation(
            summary = "Get my rating for a movie",
            description = "Get authenticated user's rating for specific movie"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Rating retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Rating not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<RatingResponseDto> getMyRatingForMovie(
            @Parameter(description = "Movie ID")
            @PathVariable UUID movieId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Optional<RatingResponseDto> rating = ratingService.getUserRatingForMovie(movieId, currentUser.getId());

        return rating.map(r -> ResponseEntity.status(HttpStatus.OK).body(r))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/movie/{movieId}")
    @Operation(
            summary = "Get all ratings for a movie",
            description = "Get all user ratings for specific movie"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Ratings retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Movie not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public ResponseEntity<List<RatingResponseDto>> getMovieRatings(
            @Parameter(description = "Movie ID")
            @PathVariable UUID movieId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<RatingResponseDto> ratings = ratingService.getMovieRatings(movieId);

        return ResponseEntity.ok(ratings);
    }
}
