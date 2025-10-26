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
public class UserResponseDto {

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "User email", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Account creation timestamp", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;
}
