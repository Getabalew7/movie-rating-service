package com.sky.movieratingservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Movie information response")
public class MovieResponseDto {
    @Schema(description = "Movie ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Movie name", example = "The Shawshank Redemption")
    private String name;

    @Schema(description = "Movie description")
    private String description;

    @Schema(description = "Release year", example = "1994")
    private Integer releaseYear;

    @Schema(description = "Genre", example = "Drama")
    private String genre;

    @Schema(description = "Director", example = "Frank Darabont")
    private String director;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
