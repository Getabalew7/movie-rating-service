package com.sky.movieratingservice.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Create Movie Request DTO")
public class CreateMovieRequestDto {
    @Schema(description = "Movie name", example = "Inception", required = true)
    @NotNull(message = "Movie name cannot be null")
    @Size(max = 100, message = "Movie name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Movie description", example = "A mind-bending thriller about dreams within dreams.")
    @Size(max = 100, message = "Description cannot exceed 100 characters")
    private String description;

    @Schema(description = "Movie genre", example = "Science Fiction")
    @Size(max = 50, message = "Genre cannot exceed 50 characters")
    private String genre;

    @Schema(description = "Movie director", example = "Christopher Nolan", required = true)
    @NotNull(message = "Director cannot be null")
    @Size(max = 100, message = "Director name cannot exceed 100 characters")
    private String director;

    @Schema(description = "Movie release year", example = "2010", required = true)
    @Size(min = 1888, max = 2025, message = "Release year must be between 1888 and 2025")
    @NotNull(message = "Release year cannot be null")
    private Integer releaseYear;
}
