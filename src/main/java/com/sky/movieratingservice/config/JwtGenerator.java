package com.sky.movieratingservice.config;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtGenerator {
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationInMs;

    public String generateJwtToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String email) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(java.util.Date.from(java.time.Instant.now()))
                .setExpiration(java.util.Date.from(java.time.Instant.now().plusMillis(jwtExpirationInMs)))
                .signWith(SECRET_KEY)
                .compact();
    }

}
