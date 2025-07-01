package edu.hm.cs.kreisel_backend.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;
    private String token;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        userDetails = User.builder()
                .username("test@hm.edu")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Generate a token to use in tests
        token = jwtUtil.generateToken(userDetails);
    }

    @Test
    void extractUsername_ShouldReturnSubjectFromToken() {
        // When
        String username = jwtUtil.extractUsername(token);

        // Then
        assertEquals("test@hm.edu", username);
    }

    @Test
    void extractExpiration_ShouldReturnExpirationDateFromToken() {
        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should be in the future
    }

    @Test
    void extractClaim_ShouldExtractSpecificClaimFromToken() {
        // When
        Function<Claims, String> subjectExtractor = Claims::getSubject;
        String subject = jwtUtil.extractClaim(token, subjectExtractor);

        // Then
        assertEquals("test@hm.edu", subject);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // When
        String generatedToken = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(generatedToken);
        // Should extract the username correctly
        assertEquals("test@hm.edu", jwtUtil.extractUsername(generatedToken));
        // Token should not be expired
        assertFalse(jwtUtil.extractExpiration(generatedToken).before(new Date()));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // When
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithDifferentUsername_ShouldReturnFalse() {
        // Given
        UserDetails differentUser = User.builder()
                .username("different@hm.edu")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Then
        assertFalse(isValid);
    }
}