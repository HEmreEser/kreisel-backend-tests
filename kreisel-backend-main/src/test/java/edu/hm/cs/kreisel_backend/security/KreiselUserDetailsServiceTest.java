package edu.hm.cs.kreisel_backend.security;

import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KreiselUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private KreiselUserDetailsService userDetailsService;

    private User regularUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Set up regular user
        regularUser = new User();
        regularUser.setId(1L);
        regularUser.setEmail("user@hm.edu");
        regularUser.setPassword("encoded_password");
        regularUser.setRole(User.Role.USER);

        // Set up admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@hm.edu");
        adminUser.setPassword("admin_password");
        adminUser.setRole(User.Role.ADMIN);
    }

    @Test
    void loadUserByUsername_WithRegularUser_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByEmail("user@hm.edu")).thenReturn(Optional.of(regularUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("user@hm.edu");

        // Then
        assertNotNull(userDetails);
        assertEquals("user@hm.edu", userDetails.getUsername());
        assertEquals("encoded_password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_WithAdminUser_ShouldReturnUserDetailsWithAdminRole() {
        // Given
        when(userRepository.findByEmail("admin@hm.edu")).thenReturn(Optional.of(adminUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@hm.edu");

        // Then
        assertNotNull(userDetails);
        assertEquals("admin@hm.edu", userDetails.getUsername());
        assertEquals("admin_password", userDetails.getPassword());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ShouldThrowException() {
        // Given
        String nonExistentEmail = "nonexistent@hm.edu";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When/Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(nonExistentEmail);
        });

        assertEquals("User not found with email: " + nonExistentEmail, exception.getMessage());
    }
}