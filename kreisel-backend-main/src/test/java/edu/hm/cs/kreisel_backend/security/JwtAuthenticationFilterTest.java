package edu.hm.cs.kreisel_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private KreiselUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;
    private String jwt = "valid.jwt.token";

    @BeforeEach
    void setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();

        // Setup common user details
        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@hm.edu")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @AfterEach
    void tearDown() {
        // Clear security context after each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidJwtCookie_ShouldAuthenticateUser() throws Exception {
        // Given
        Cookie jwtCookie = new Cookie("jwt", jwt);
        Cookie[] cookies = { jwtCookie };

        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.extractUsername(jwt)).thenReturn("test@hm.edu");
        when(userDetailsService.loadUserByUsername("test@hm.edu")).thenReturn(userDetails);
        when(jwtUtil.validateToken(jwt, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithValidAuthorizationHeader_ShouldAuthenticateUser() throws Exception {
        // Given
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtil.extractUsername(jwt)).thenReturn("test@hm.edu");
        when(userDetailsService.loadUserByUsername("test@hm.edu")).thenReturn(userDetails);
        when(jwtUtil.validateToken(jwt, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoJwt_ShouldNotAuthenticate() throws Exception {
        // Given
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }

    @Test
    void doFilterInternal_WithInvalidJwt_ShouldNotAuthenticate() throws Exception {
        // Given
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtil.extractUsername(jwt)).thenReturn("test@hm.edu");
        when(userDetailsService.loadUserByUsername("test@hm.edu")).thenReturn(userDetails);
        when(jwtUtil.validateToken(jwt, userDetails)).thenReturn(false); // Invalid token

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithJwtExtractionException_ShouldContinueChain() throws Exception {
        // Given
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtil.extractUsername(jwt)).thenThrow(new RuntimeException("Invalid token"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilterInternal_WithMultipleCookies_ShouldFindJwtCookie() throws Exception {
        // Given
        Cookie sessionCookie = new Cookie("session", "session-value");
        Cookie jwtCookie = new Cookie("jwt", jwt);
        Cookie preferenceCookie = new Cookie("preference", "dark-mode");
        Cookie[] cookies = { sessionCookie, jwtCookie, preferenceCookie };

        when(request.getCookies()).thenReturn(cookies);
        when(jwtUtil.extractUsername(jwt)).thenReturn("test@hm.edu");
        when(userDetailsService.loadUserByUsername("test@hm.edu")).thenReturn(userDetails);
        when(jwtUtil.validateToken(jwt, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithMalformedAuthorizationHeader_ShouldNotAuthenticate() throws Exception {
        // Given
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Malformed Header");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userDetailsService);
    }
}