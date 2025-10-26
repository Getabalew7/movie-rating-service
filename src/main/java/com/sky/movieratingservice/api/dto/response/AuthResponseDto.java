package com.sky.movieratingservice.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Authentication Response With JWT token")
public class AuthResponseDto {

    @Schema( description = "JWT Access Token", example = "eyJhbGciOi ...")
    private String accessToken;

    @Schema(description = "Type of the token", example = "Bearer")
    @Builder.Default
    private String tokenType ="Bearer";

    @Schema(description = "Expiration time in seconds", example = "3600")
    private Long expiresIn;

    @Schema(description = "User details associated with the token")
    private UserResponseDto userResponseDto;
}
