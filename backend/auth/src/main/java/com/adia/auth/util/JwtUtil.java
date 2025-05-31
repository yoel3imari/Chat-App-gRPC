package com.adia.auth.util;

import com.adia.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;


@Component
public class JwtUtil {

    @Value("${jwt.secret:MdMa4RidWdbS2qJQP69LmMAJW8dRuFHbitgWqxFqWh4=}")
    private String jwtSecret;

    private final MacAlgorithm alg = Jwts.SIG.HS256;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Generate token with subject (e.g., email)
    public String generateToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60 * 60)))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Validate token
    public boolean isTokenValid(String token, String expectedSubject) {
        try {
            String subject = extractSubject(token);
            return subject.equals(expectedSubject);
        } catch (Exception e) {
            return false;
        }
    }
}

