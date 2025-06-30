package edu.hm.cs.kreisel_backend.config;

import edu.hm.cs.kreisel_backend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private AuthenticationConfiguration authenticationConfiguration;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = mock(JwtAuthenticationFilter.class);
        authenticationConfiguration = mock(AuthenticationConfiguration.class);
        securityConfig = new SecurityConfig(jwtAuthenticationFilter);
    }

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // When
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // Then
        assertTrue(encoder instanceof BCryptPasswordEncoder);

        // Test encoding behavior
        String password = "testPassword";
        String encodedPassword = encoder.encode(password);

        // Assert encoding works and is not plaintext
        assertNotEquals(password, encodedPassword);
        assertTrue(encoder.matches(password, encodedPassword));
        assertFalse(encoder.matches("wrongPassword", encodedPassword));
    }

    @Test
    void authenticationManager_ShouldReturnFromConfiguration() throws Exception {
        // Given
        AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockAuthManager);

        // When
        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        // Then
        assertNotNull(result);
        assertEquals(mockAuthManager, result);
    }

    @Test
    void corsConfigurationSource_ShouldConfigureCorsCorrectly() {
        // When
        CorsConfigurationSource corsConfigSource = securityConfig.corsConfigurationSource();

        // Then
        assertNotNull(corsConfigSource);

        // Extract the CorsConfiguration (assuming we have access to the underlying implementation)
        UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) corsConfigSource;
        CorsConfiguration corsConfig = source.getCorsConfigurations().get("/**");

        // Verify CORS settings
        assertNotNull(corsConfig);
        assertEquals(List.of("*"), corsConfig.getAllowedOriginPatterns());
        assertTrue(corsConfig.getAllowedMethods().containsAll(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")));
        assertEquals(List.of("*"), corsConfig.getAllowedHeaders());
        assertTrue(corsConfig.getAllowCredentials());
    }

    // We'll skip testing filterChain directly since it's difficult to mock HttpSecurity
    // Instead, we could test this in an integration test
}