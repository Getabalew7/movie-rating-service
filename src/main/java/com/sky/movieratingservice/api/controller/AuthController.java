package com.sky.movieratingservice.api.controller;

import com.sky.movieratingservice.api.dto.request.UserLoginRequestDto;
import com.sky.movieratingservice.api.dto.request.UserRegistrationRequestDto;
import com.sky.movieratingservice.api.dto.response.AuthResponseDto;
import com.sky.movieratingservice.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/auth", produces = "application/json")
@RequiredArgsConstructor
@Validated
@Tag(name = "Authentication Controller", description = "Endpoints for user Registration and authorization")
public class AuthController {

    private final IAuthService authService;

    @PostMapping(value = "/register", consumes = "application/json")
    @Operation(
            summary = "Register a new user",
            description = "Endpoint to register a new user in the system."
    )
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data or email already in use"),
            }
    )
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody UserRegistrationRequestDto userRegistrationRequestDto){
        AuthResponseDto authResponseDto = authService.register(userRegistrationRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseDto);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "User login",
            description = "Authenticate User and return a JWT token."
    )
    @ApiResponses(
            value = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User authenticated successfully"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
            }
    )
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        AuthResponseDto response = authService.login(userLoginRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
