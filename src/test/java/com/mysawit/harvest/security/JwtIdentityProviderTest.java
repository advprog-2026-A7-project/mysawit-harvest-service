package com.mysawit.harvest.security;

import com.mysawit.harvest.dto.AuthenticatedUser;
import com.mysawit.harvest.exception.UnauthorizedUserException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtIdentityProviderTest {
    private JwtIdentityProvider jwtIdentityProvider;
    private final String secret = "secretKeyYangSangatPanjangMinimal32Karakter!!";
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtIdentityProvider = new JwtIdentityProvider();
        ReflectionTestUtils.setField(jwtIdentityProvider, "jwtSecret", secret);
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private String createToken(String userId, String role, long expirationMillis) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Success: Should return AuthenticatedUser when token is valid")
    void getAuthenticatedUser_Success() {
        String userId = UUID.randomUUID().toString();
        String role = "BURUH";
        String token = createToken(userId, role, 60000);
        String header = "Bearer " + token;

        AuthenticatedUser result = jwtIdentityProvider.getAuthenticatedUser(header);

        assertNotNull(result);
        assertEquals(userId, result.id().toString());
        assertEquals(role, result.role());
    }

    @Test
    @DisplayName("Fail: Should throw exception when header is null")
    void getAuthenticatedUser_NullHeader() {
        assertThrows(UnauthorizedUserException.class, () ->
                        jwtIdentityProvider.getAuthenticatedUser(null),
                "Authorization bearer token is required."
        );
    }

    @Test
    @DisplayName("Fail: Should throw exception when header doesn't start with Bearer")
    void getAuthenticatedUser_NoBearerPrefix() {
        assertThrows(UnauthorizedUserException.class, () ->
                jwtIdentityProvider.getAuthenticatedUser("Basic bWFkb3I6cGFzcw==")
        );
    }

    @Test
    @DisplayName("Fail: Should throw exception when token is invalid/corrupt")
    void getAuthenticatedUser_InvalidToken() {
        String header = "Bearer ini.token.ngasal";

        assertThrows(UnauthorizedUserException.class, () ->
                        jwtIdentityProvider.getAuthenticatedUser(header),
                "Invalid or expired token."
        );
    }

    @Test
    @DisplayName("Fail: Should throw exception when token is expired")
    void getAuthenticatedUser_ExpiredToken() {
        String token = createToken(UUID.randomUUID().toString(), "MANDOR", -1000);
        String header = "Bearer " + token;

        assertThrows(UnauthorizedUserException.class, () ->
                jwtIdentityProvider.getAuthenticatedUser(header)
        );
    }

    @Test
    @DisplayName("Fail: Should throw exception when userId claim is missing")
    void getAuthenticatedUser_MissingUserId() {
        String token = Jwts.builder()
                .claim("role", "BURUH")
                .signWith(key)
                .compact();
        String header = "Bearer " + token;

        UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
                jwtIdentityProvider.getAuthenticatedUser(header)
        );
        assertEquals("Invalid token claims.", ex.getMessage());
    }

    @Test
    @DisplayName("Fail: Should throw exception when role claim is missing")
    void getAuthenticatedUser_MissingRole() {
        String token = Jwts.builder()
                .claim("userId", UUID.randomUUID().toString())
                .signWith(key)
                .compact();
        String header = "Bearer " + token;

        UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
                jwtIdentityProvider.getAuthenticatedUser(header)
        );
        assertEquals("Invalid token claims.", ex.getMessage());
    }
}