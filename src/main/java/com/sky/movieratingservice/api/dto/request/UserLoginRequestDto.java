package com.sky.movieratingservice.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "User Login Request DTO")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserLoginRequestDto {
    @Schema(description = "User email address", example = "test@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "User password", example = "P@ssw0rd!")
    @NotBlank(message = "Password cannot be blank")
    private String password;
}
