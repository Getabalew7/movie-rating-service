package com.sky.movieratingservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Movie Rating Service API",
                version = "1.0",
                description = """
                        RESTful API for movie rating service.
                        
                            ## Features
                            - User registration and authentication (JWT)
                            - Browse movies (public)
                            - Rate movies (authenticated users)
                            - Update and delete ratings
                            - View top-rated movies
                
                            ## Authentication
                            Most endpoints require authentication. To authenticate:
                            1. Register via POST /api/v1/auth/register
                            2. Login via POST /api/v1/auth/login to get JWT token
                            3. Click 'Authorize' button and enter: Bearer YOUR_TOKEN
                
                            Test credentials (development):
                            - Email: test@example.com
                            - Password: Test123!@#
                        """,
                contact = @Contact(
                        name = "Movie Rating Service Support",
                        email = "dev@skySupportTeam"
                )
        ),
        servers = {
                @io.swagger.v3.oas.annotations.servers.Server(
                        url = "http://localhost:8080",
                        description = "Local Development Server"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        description = "Enter JWT token obtained from /api/v1/auth/login in the format" +
                " 'Bearer YOUR_TOKEN'"
)
public class OpenApiConfig {
}
