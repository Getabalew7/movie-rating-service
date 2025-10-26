package com.sky.movieratingservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Authentication Response Data Transfer Object")
public class RatingResponseDto {
    @Schema(description = "Rating ID")
    private UUID id;

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "User email")
    private String userEmail;

    @Schema(description = "Movie ID")
    private UUID movieId;

    @Schema(description = "Movie name")
    private String movieName;

    @Schema(description = "Rating value (1-10)", example = "9")
    private Integer ratingValue;

    @Schema(description = "Optional review text")
    private String review;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
