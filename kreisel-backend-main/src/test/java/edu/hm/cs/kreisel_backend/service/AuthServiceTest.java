package edu.hm.cs.kreisel_backend.service;

import edu.hm.cs.kreisel_backend.dto.AuthResponse;
import edu.hm.cs.kreisel_backend.dto.LoginRequest;
import edu.hm.cs.kreisel_backend.dto.RegisterRequest;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.repository.UserRepository;
import edu.hm.cs.kreisel_backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Setup common test data
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test User");
        registerRequest.setEmail("test@hm.edu");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@hm.edu");
        loginRequest.setPassword("password123");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFullName("Test User");
        savedUser.setEmail("test@hm.edu");
        savedUser.setPassword("encoded_password");
        savedUser.setRole(User.Role.USER);

        jwtToken = "test-jwt-token";
    }

    @Test
    void register_WithValidHmEmail_ShouldRegisterUser() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertEquals(1L, response.getUserId());
        assertEquals("test@hm.edu", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals("USER", response.getRole());
        assertEquals("Registrierung erfolgreich", response.getMessage());
        assertEquals(jwtToken, response.getToken());

        // Verify user was saved with correct data
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("Test User", capturedUser.getFullName());
        assertEquals("test@hm.edu", capturedUser.getEmail());
        assertEquals("encoded_password", capturedUser.getPassword());
        assertEquals(User.Role.USER, capturedUser.getRole());

        verify(userRepository).existsByEmail("test@hm.edu");
        verify(passwordEncoder).encode("password123");
        verify(jwtUtil).generateToken(any(UserDetails.class));
    }

    @Test
    void register_WithAdminEmail_ShouldRegisterAdminUser() {
        // Given
        RegisterRequest adminRegisterRequest = new RegisterRequest();
        adminRegisterRequest.setFullName("Admin User");
        adminRegisterRequest.setEmail("admin@hm.edu");
        adminRegisterRequest.setPassword("admin123");

        User savedAdminUser = new User();
        savedAdminUser.setId(2L);
        savedAdminUser.setFullName("Admin User");
        savedAdminUser.setEmail("admin@hm.edu");
        savedAdminUser.setPassword("encoded_admin_password");
        savedAdminUser.setRole(User.Role.ADMIN);

        when(userRepository.existsByEmail(adminRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(adminRegisterRequest.getPassword())).thenReturn("encoded_admin_password");
        when(userRepository.save(any(User.class))).thenReturn(savedAdminUser);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.register(adminRegisterRequest);

        // Then
        assertEquals(2L, response.getUserId());
        assertEquals("admin@hm.edu", response.getEmail());
        assertEquals("Admin User", response.getFullName());
        assertEquals("ADMIN", response.getRole());
        assertEquals(jwtToken, response.getToken());

        // Verify user was saved with correct data and admin role
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(User.Role.ADMIN, capturedUser.getRole());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email bereits registriert", exception.getMessage());
        verify(userRepository).existsByEmail("test@hm.edu");
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void register_WithNonHmEmail_ShouldThrowException() {
        // Given
        registerRequest.setEmail("test@gmail.com");
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Nur HM-E-Mail-Adressen sind erlaubt", exception.getMessage());
        verify(userRepository).existsByEmail("test@gmail.com");
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void login_WithValidCredentials_ShouldLoginSuccessfully() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), savedUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertEquals(1L, response.getUserId());
        assertEquals("test@hm.edu", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals("USER", response.getRole());
        assertEquals("Login erfolgreich", response.getMessage());
        assertEquals(jwtToken, response.getToken());

        verify(userRepository).findByEmail("test@hm.edu");
        verify(passwordEncoder).matches("password123", "encoded_password");
        verify(jwtUtil).generateToken(any(UserDetails.class));
    }

    @Test
    void login_WithNonExistentEmail_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Email oder Passwort falsch", exception.getMessage());
        verify(userRepository).findByEmail("test@hm.edu");
        verifyNoInteractions(passwordEncoder, jwtUtil);
    }

    @Test
    void login_WithIncorrectPassword_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), savedUser.getPassword())).thenReturn(false);

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Email oder Passwort falsch", exception.getMessage());
        verify(userRepository).findByEmail("test@hm.edu");
        verify(passwordEncoder).matches("password123", "encoded_password");
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_WithAdminUser_ShouldReturnAdminRole() {
        // Given
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setFullName("Admin User");
        adminUser.setEmail("admin@hm.edu");
        adminUser.setPassword("encoded_admin_password");
        adminUser.setRole(User.Role.ADMIN);

        LoginRequest adminLoginRequest = new LoginRequest();
        adminLoginRequest.setEmail("admin@hm.edu");
        adminLoginRequest.setPassword("admin123");

        when(userRepository.findByEmail(adminLoginRequest.getEmail())).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches(adminLoginRequest.getPassword(), adminUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn(jwtToken);

        // When
        AuthResponse response = authService.login(adminLoginRequest);

        // Then
        assertEquals(2L, response.getUserId());
        assertEquals("admin@hm.edu", response.getEmail());
        assertEquals("Admin User", response.getFullName());
        assertEquals("ADMIN", response.getRole());
        assertEquals(jwtToken, response.getToken());
    }
}