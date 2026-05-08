package com.mysawit.harvest.security;

import com.mysawit.harvest.dto.AuthenticatedUser;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class JwtIdentityProvider {
    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthenticatedUser getAuthenticatedUser(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        Claims claims = parseClaims(token);

        String userId = claims.get("userId", String.class);
        String role = claims.get("role", String.class);

        if (userId == null || role == null) {
            throw new UnauthorizedUserException("Invalid token claims.");
        }

        return new AuthenticatedUser(UUID.fromString(userId), role);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw new UnauthorizedUserException("Invalid or expired token.");
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedUserException("Authorization bearer token is required.");
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
