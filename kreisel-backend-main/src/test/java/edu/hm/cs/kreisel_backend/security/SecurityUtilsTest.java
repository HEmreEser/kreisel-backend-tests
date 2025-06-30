package edu.hm.cs.kreisel_backend.security;

import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityUtils securityUtils;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@hm.edu");
        testUser.setFullName("Test User");

        // Set up security context mock
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        // Clear security context after each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WhenAuthenticatedWithUserDetails_ShouldReturnUser() {
        // Given
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("test@hm.edu")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail("test@hm.edu")).thenReturn(Optional.of(testUser));

        // When
        User result = securityUtils.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository).findByEmail("test@hm.edu");
    }

    @Test
    void getCurrentUser_WhenNotAuthenticated_ShouldReturnNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        User result = securityUtils.getCurrentUser();

        // Then
        assertNull(result);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getCurrentUser_WhenAuthenticationNotAuthenticated_ShouldReturnNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        User result = securityUtils.getCurrentUser();

        // Then
        assertNull(result);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getCurrentUser_WhenPrincipalIsNotUserDetails_ShouldReturnNull() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("not-a-user-details-object");

        // When
        User result = securityUtils.getCurrentUser();

        // Then
        assertNull(result);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getCurrentUser_WhenUserNotFound_ShouldReturnNull() {
        // Given
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("unknown@hm.edu")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail("unknown@hm.edu")).thenReturn(Optional.empty());

        // When
        User result = securityUtils.getCurrentUser();

        // Then
        assertNull(result);
        verify(userRepository).findByEmail("unknown@hm.edu");
    }

    @Test
    void isAuthenticated_WhenAuthenticated_ShouldReturnTrue() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // When
        boolean result = securityUtils.isAuthenticated();

        // Then
        assertTrue(result);
    }

    @Test
    void isAuthenticated_WhenNotAuthenticated_ShouldReturnFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        boolean result = securityUtils.isAuthenticated();

        // Then
        assertFalse(result);
    }

    @Test
    void isAuthenticated_WhenAuthenticationIsNull_ShouldReturnFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        boolean result = securityUtils.isAuthenticated();

        // Then
        assertFalse(result);
    }
}