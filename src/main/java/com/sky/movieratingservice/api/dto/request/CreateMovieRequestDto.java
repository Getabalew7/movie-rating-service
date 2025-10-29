package com.sky.movieratingservice.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Size(min = 4, message = "Movie name must be at least 4 characters long")
    private String name;

    @Schema(description = "Movie description", example = "A mind-bending thriller about dreams within dreams.")
    @Size(max = 100, message = "Description cannot exceed 100 characters")
    private String description;

    @Schema(description = "Movie genre", example = "Science Fiction")
    @Size(max = 50, message = "Genre cannot exceed 50 characters")
    @Size(min = 4, message = "Genre must be at least 4 characters long")
    @NotNull(message = "Genre cannot be null")
    private String genre;

    @Schema(description = "Movie director", example = "Christopher Nolan", required = true)
    @NotNull(message = "Director cannot be null")
    @Size(max = 100, message = "Director name cannot exceed 100 characters")
    private String director;

    @Schema(description = "Movie release year", example = "2010", required = true)
    @NotNull(message = "Release year cannot be null")
    @Min(value =  1888, message = "Release year cannot be less than 1888")
    @Max(value = 2500, message = "Release year cannot be greater than 2500")
    private Integer releaseYear;
}
