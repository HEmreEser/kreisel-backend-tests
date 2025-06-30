package edu.hm.cs.kreisel_backend.controller;

import edu.hm.cs.kreisel_backend.dto.AuthResponse;
import edu.hm.cs.kreisel_backend.dto.LoginRequest;
import edu.hm.cs.kreisel_backend.dto.RegisterRequest;
import edu.hm.cs.kreisel_backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletResponse httpResponse;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
        registerRequest.setEmail("test@hm.edu");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@hm.edu");
        loginRequest.setPassword("password123");

        authResponse = AuthResponse.builder()
                .userId(1L)
                .email("test@hm.edu")
                .fullName("Test User")
                .role("USER")
                .message("Authentication successful")
                .token("jwt-token-12345")
                .build();
    }

    @Test
    void register_ShouldRegisterUserAndAddCookie() {
        // Given
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // When
        ResponseEntity<AuthResponse> response = authController.register(registerRequest, httpResponse);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());

        // Verify authService was called with correct request
        verify(authService).register(registerRequest);

        // Verify cookie was added
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpResponse).addCookie(cookieCaptor.capture());

        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals("jwt", capturedCookie.getName());
        assertEquals("jwt-token-12345", capturedCookie.getValue());
        assertEquals(10 * 60 * 60, capturedCookie.getMaxAge()); // 10 hours in seconds
        assertEquals("/", capturedCookie.getPath());
        assertTrue(capturedCookie.isHttpOnly());
    }

    @Test
    void login_ShouldLoginUserAndAddCookie() {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When
        ResponseEntity<AuthResponse> response = authController.login(loginRequest, httpResponse);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());

        // Verify authService was called with correct request
        verify(authService).login(loginRequest);

        // Verify cookie was added
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpResponse).addCookie(cookieCaptor.capture());

        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals("jwt", capturedCookie.getName());
        assertEquals("jwt-token-12345", capturedCookie.getValue());
        assertEquals(10 * 60 * 60, capturedCookie.getMaxAge()); // 10 hours in seconds
        assertEquals("/", capturedCookie.getPath());
        assertTrue(capturedCookie.isHttpOnly());
    }

    @Test
    void logout_ShouldClearJWTCookie() {
        // When
        ResponseEntity<String> response = authController.logout(httpResponse);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Logout erfolgreich", response.getBody());

        // Verify cookie was invalidated
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpResponse).addCookie(cookieCaptor.capture());

        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals("jwt", capturedCookie.getName());
        assertNull(capturedCookie.getValue());
        assertEquals(0, capturedCookie.getMaxAge()); // Expire immediately
        assertEquals("/", capturedCookie.getPath());
        assertTrue(capturedCookie.isHttpOnly());
    }

    @Test
    void addTokenCookie_ShouldCreateProperCookie() {
        // Create a custom token for testing the private method through public methods
        String customToken = "custom-test-token";
        AuthResponse customResponse = AuthResponse.builder()
                .token(customToken)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(customResponse);

        // Call a public method that uses the private method
        authController.login(loginRequest, httpResponse);

        // Verify cookie was created correctly
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpResponse).addCookie(cookieCaptor.capture());

        Cookie capturedCookie = cookieCaptor.getValue();
        assertEquals("jwt", capturedCookie.getName());
        assertEquals(customToken, capturedCookie.getValue());
        assertEquals(10 * 60 * 60, capturedCookie.getMaxAge());
        assertEquals("/", capturedCookie.getPath());
        assertTrue(capturedCookie.isHttpOnly());
    }
}