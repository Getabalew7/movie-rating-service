package com.sky.movieratingservice.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jdk.jfr.Name;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Schema(description = "Movie Rating Request DTO")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingRequestDto {
    @Schema(description = "Movie ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @NotNull(message = "Movie ID cannot be null")
    private UUID movieId;

    @Schema(description = "Rating value (1 to 10)", example = "4", required = true)
    @NotNull(message = "Rating value cannot be null")
    @Min(value = 1, message = "Rating value must be at least 1")
    @Max(value = 10, message = "Rating value must be at most 10")
    private Integer ratingValue;

    @Schema(description = "Optional review text", example = "Great movie with stunning visuals")
    @Size(max = 2000, message = "Review cannot exceed 2000 characters")
    private String review;
}
