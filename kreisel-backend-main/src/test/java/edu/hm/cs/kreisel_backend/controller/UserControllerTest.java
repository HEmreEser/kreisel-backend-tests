package edu.hm.cs.kreisel_backend.controller;

import edu.hm.cs.kreisel_backend.model.Rental;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.security.SecurityUtils;
import edu.hm.cs.kreisel_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private User adminUser;
    private Rental testRental;
    private List<User> userList;
    private List<Rental> rentalList;

    @BeforeEach
    void setUp() {
        // Setup normal user
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("user@hm.edu");
        testUser.setPassword("password123");
        testUser.setRole(User.Role.USER);

        // Setup admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setFullName("Admin User");
        adminUser.setEmail("admin@hm.edu");
        adminUser.setPassword("adminpass");
        adminUser.setRole(User.Role.ADMIN);

        // Setup test rental
        testRental = new Rental();
        testRental.setId(1L);
        testRental.setUser(testUser);
        testRental.setRentalDate(LocalDate.now());
        testRental.setEndDate(LocalDate.now().plusDays(7));

        // Setup lists
        userList = Arrays.asList(testUser, adminUser);
        rentalList = Arrays.asList(testRental);
    }

    // Tests for admin endpoints

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        when(userService.getAllUsers()).thenReturn(userList);

        // When
        List<User> result = userController.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertEquals(testUser, result.get(0));
        assertEquals(adminUser, result.get(1));
        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_ShouldReturnUser() {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUser);

        // When
        User result = userController.getUserById(1L);

        // Then
        assertEquals(testUser, result);
        verify(userService).getUserById(1L);
    }

    @Test
    void getUserByEmail_ShouldReturnUser() {
        // Given
        String email = "user@hm.edu";
        when(userService.getUserByEmail(email)).thenReturn(testUser);

        // When
        User result = userController.getUserByEmail(email);

        // Then
        assertEquals(testUser, result);
        verify(userService).getUserByEmail(email);
    }

    @Test
    void getUserRentals_ShouldReturnUserRentals() {
        // Given
        when(userService.getRentalsByUserId(1L)).thenReturn(rentalList);

        // When
        List<Rental> result = userController.getUserRentals(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(testRental, result.get(0));
        verify(userService).getRentalsByUserId(1L);
    }

    @Test
    void createUser_ShouldCreateAndReturnUser() {
        // Given
        User newUser = new User();
        newUser.setFullName("New User");
        newUser.setEmail("new@hm.edu");

        when(userService.createUser(newUser)).thenReturn(newUser);

        // When
        User result = userController.createUser(newUser);

        // Then
        assertEquals(newUser, result);
        verify(userService).createUser(newUser);
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() {
        // Given
        User updatedUser = new User();
        updatedUser.setFullName("Updated User");
        updatedUser.setEmail("user@hm.edu");

        when(userService.updateUser(1L, updatedUser)).thenReturn(updatedUser);

        // When
        User result = userController.updateUser(1L, updatedUser);

        // Then
        assertEquals(updatedUser, result);
        verify(userService).updateUser(1L, updatedUser);
    }

    @Test
    void deleteUser_ShouldCallServiceDelete() {
        // When
        userController.deleteUser(1L);

        // Then
        verify(userService).deleteUser(1L);
    }

    // Tests for current user endpoints

    @Test
    void getCurrentUser_WhenAuthenticated_ShouldReturnCurrentUser() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);

        // When
        ResponseEntity<User> response = userController.getCurrentUser();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
        verify(securityUtils).getCurrentUser();
    }

    @Test
    void getCurrentUser_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<User> response = userController.getCurrentUser();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(securityUtils).getCurrentUser();
    }

    @Test
    void updateCurrentUser_WhenAuthenticated_ShouldUpdateUser() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);

        User updatedUser = new User();
        updatedUser.setFullName("Updated Name");
        updatedUser.setEmail("updated@hm.edu");
        // Try to change role (should be prevented)
        updatedUser.setRole(User.Role.ADMIN);

        User expectedSavedUser = new User();
        expectedSavedUser.setId(1L);
        expectedSavedUser.setFullName("Updated Name");
        expectedSavedUser.setEmail("updated@hm.edu");
        expectedSavedUser.setRole(User.Role.USER); // Role should not change

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(expectedSavedUser);

        // When
        ResponseEntity<User> response = userController.updateCurrentUser(updatedUser);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSavedUser, response.getBody());

        // Verify that role was preserved from original user
        verify(securityUtils).getCurrentUser();
        verify(userService).updateUser(eq(1L), argThat(user ->
                user.getRole() == User.Role.USER && // Role should be preserved
                        user.getFullName().equals("Updated Name") && // Name should be updated
                        user.getEmail().equals("updated@hm.edu") // Email should be updated
        ));
    }

    @Test
    void updateCurrentUser_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);
        User updatedUser = new User();

        // When
        ResponseEntity<User> response = userController.updateCurrentUser(updatedUser);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(userService);
    }

    @Test
    void updateCurrentUserName_WithValidName_ShouldUpdateName() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        Map<String, String> request = new HashMap<>();
        request.put("fullName", "New Name");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setFullName("New Name");
        updatedUser.setEmail("user@hm.edu");
        updatedUser.setRole(User.Role.USER);

        when(userService.updateUserName(1L, "New Name")).thenReturn(updatedUser);

        // When
        ResponseEntity<User> response = userController.updateCurrentUserName(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUser, response.getBody());
        verify(securityUtils).getCurrentUser();
        verify(userService).updateUserName(1L, "New Name");
    }

    @Test
    void updateCurrentUserName_WithEmptyName_ShouldReturnBadRequest() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        Map<String, String> request = new HashMap<>();
        request.put("fullName", "  ");

        // When
        ResponseEntity<User> response = userController.updateCurrentUserName(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(userService);
    }

    @Test
    void updateCurrentUserName_WithMissingName_ShouldReturnBadRequest() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        Map<String, String> request = new HashMap<>();
        // No name provided

        // When
        ResponseEntity<User> response = userController.updateCurrentUserName(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(userService);
    }

    @Test
    void updateCurrentUserName_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);
        Map<String, String> request = new HashMap<>();
        request.put("fullName", "New Name");

        // When
        ResponseEntity<User> response = userController.updateCurrentUserName(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(userService);
    }

    @Test
    void updateCurrentUserPassword_WithValidPasswords_ShouldUpdatePassword() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", "password123");
        request.put("newPassword", "newPassword123");

        doNothing().when(userService).updateUserPassword(1L, "password123", "newPassword123");

        // When
        ResponseEntity<Void> response = userController.updateCurrentUserPassword(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verify(userService).updateUserPassword(1L, "password123", "newPassword123");
    }

    @Test
    void updateCurrentUserPassword_WithShortNewPassword_ShouldReturnBadRequest() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", "password123");
        request.put("newPassword", "short"); // Less than 6 characters

        // When
        ResponseEntity<Void> response = userController.updateCurrentUserPassword(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(userService);
    }

    @Test
    void updateCurrentUserPassword_WithIncorrectCurrentPassword_ShouldReturnBadRequest() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", "wrongPassword");
        request.put("newPassword", "newPassword123");

        doThrow(new IllegalArgumentException("Current password is incorrect"))
                .when(userService).updateUserPassword(1L, "wrongPassword", "newPassword123");

        // When
        ResponseEntity<Void> response = userController.updateCurrentUserPassword(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verify(userService).updateUserPassword(1L, "wrongPassword", "newPassword123");
    }

    @Test
    void updateCurrentUserPassword_WithMissingParameters_ShouldReturnBadRequest() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", "password123");
        // Missing newPassword

        // When
        ResponseEntity<Void> response = userController.updateCurrentUserPassword(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(userService);
    }

    @Test
    void updateCurrentUserPassword_WithEmptyParameters_ShouldReturnBadRequest() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", "");
        request.put("newPassword", "newPassword123");

        // When
        ResponseEntity<Void> response = userController.updateCurrentUserPassword(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(userService);
    }

    @Test
    void updateCurrentUserPassword_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);
        Map<String, String> request = new HashMap<>();
        request.put("currentPassword", "password123");
        request.put("newPassword", "newPassword123");

        // When
        ResponseEntity<Void> response = userController.updateCurrentUserPassword(request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(userService);
    }

    @Test
    void deleteCurrentUser_WhenAuthenticated_ShouldDeleteUser() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        doNothing().when(userService).deleteUser(1L);

        // When
        ResponseEntity<Void> response = userController.deleteCurrentUser();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteCurrentUser_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<Void> response = userController.deleteCurrentUser();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(userService);
    }
}