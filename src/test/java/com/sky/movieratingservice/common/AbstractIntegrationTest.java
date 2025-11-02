package com.sky.movieratingservice.common;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.movieratingservice.api.dto.request.UserRegistrationRequestDto;
import com.sky.movieratingservice.domain.repository.MovieRepository;
import com.sky.movieratingservice.domain.repository.RatingRepository;
import com.sky.movieratingservice.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureWebTestClient
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("movierating")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.liquibase.contexts", () -> "test");
    }

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected MovieRepository movieRepository;

    @Autowired
    protected RatingRepository ratingRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected WebTestClient webClient;

    // Helper method
    protected String registerAndGetToken(String email, String password) throws Exception {
        UserRegistrationRequestDto request = UserRegistrationRequestDto.builder()
                .email(email)
                .password(password)
                .build();

        byte[] responseBody = webClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult()     // ✅ returns EntityExchangeResult<byte[]>
                .getResponseBody(); // ✅ returns byte[]

        return objectMapper.readTree(responseBody)
                .get("accessToken")
                .asText();
    }

}
