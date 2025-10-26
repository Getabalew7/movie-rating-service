package com.sky.movieratingservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Top rated movie response")
public class TopRatedMovieResponseDto {
    @Schema(description = "Movie ID")
    private UUID id;

    @Schema(description = "Movie name")
    private String name;

    @Schema(description = "Movie description")
    private String description;

    @Schema(description = "Release year")
    private Integer releaseYear;

    @Schema(description = "Genre")
    private String genre;

    @Schema(description = "Director")
    private String director;

    @Schema(description = "Average rating", example = "9.3")
    private Double avgRating;

    @Schema(description = "Total number of ratings", example = "2500")
    private Long ratingCount;
}
