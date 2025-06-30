package edu.hm.cs.kreisel_backend.service;

import edu.hm.cs.kreisel_backend.model.Rental;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private List<User> userList;
    private List<Rental> rentalList;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@hm.edu");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setRole(User.Role.USER);

        // Create test rental
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUser(testUser);
        rental.setRentalDate(LocalDate.now());
        rental.setEndDate(LocalDate.now().plusDays(7));

        // Setup rental list
        rentalList = Collections.singletonList(rental);
        testUser.setRentals(rentalList);

        // Setup user list with additional admin user
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setFullName("Admin User");
        adminUser.setEmail("admin@hm.edu");
        adminUser.setPassword("$2a$10$encodedAdminPassword");
        adminUser.setRole(User.Role.ADMIN);

        userList = Arrays.asList(testUser, adminUser);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(userList);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(testUser));
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertEquals(testUser, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserById(99L);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(99L);
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        String email = "test@hm.edu";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByEmail(email);

        // Then
        assertEquals(testUser, result);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        String nonExistentEmail = "nonexistent@hm.edu";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getUserByEmail(nonExistentEmail);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void getRentalsByUserId_WhenUserExists_ShouldReturnRentals() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        List<Rental> result = userService.getRentalsByUserId(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals(rentalList, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getRentalsByUserId_WhenUserDoesNotExist_ShouldThrowException() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.getRentalsByUserId(99L);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(99L);
    }

    @Test
    void createUser_WithNonHashedPassword_ShouldHashPasswordAndSaveUser() {
        // Given
        User newUser = new User();
        newUser.setFullName("New User");
        newUser.setEmail("new@hm.edu");
        newUser.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = userService.createUser(newUser);

        // Then
        assertEquals(newUser, result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals("$2a$10$encodedPassword", capturedUser.getPassword());
        assertEquals(User.Role.USER, capturedUser.getRole());
        verify(passwordEncoder).encode("plainPassword");
    }

    @Test
    void createUser_WithHashedPassword_ShouldNotHashAgain() {
        // Given
        User newUser = new User();
        newUser.setFullName("New User");
        newUser.setEmail("new@hm.edu");
        newUser.setPassword("$2a$10$alreadyHashedPassword");

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = userService.createUser(newUser);

        // Then
        assertEquals(newUser, result);
        verify(userRepository).save(newUser);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createUser_WithSpecifiedRole_ShouldKeepRole() {
        // Given
        User newAdminUser = new User();
        newAdminUser.setFullName("New Admin");
        newAdminUser.setEmail("newadmin@hm.edu");
        newAdminUser.setPassword("adminPassword");
        newAdminUser.setRole(User.Role.ADMIN);

        when(passwordEncoder.encode("adminPassword")).thenReturn("$2a$10$encodedAdminPassword");
        when(userRepository.save(any(User.class))).thenReturn(newAdminUser);

        // When
        User result = userService.createUser(newAdminUser);

        // Then
        assertEquals(newAdminUser, result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals(User.Role.ADMIN, capturedUser.getRole());
    }

    @Test
    void updateUser_WithFullUpdate_ShouldUpdateAllFields() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFullName("Old Name");
        existingUser.setEmail("old@hm.edu");
        existingUser.setPassword("$2a$10$oldEncodedPassword");
        existingUser.setRole(User.Role.USER);

        User updatedUser = new User();
        updatedUser.setFullName("Updated Name");
        updatedUser.setEmail("updated@hm.edu");
        updatedUser.setPassword("newPassword");
        updatedUser.setRole(User.Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.updateUser(1L, updatedUser);

        // Then
        assertEquals("Updated Name", result.getFullName());
        assertEquals("updated@hm.edu", result.getEmail());
        assertEquals("$2a$10$newEncodedPassword", result.getPassword());
        assertEquals(User.Role.ADMIN, result.getRole());

        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_WithoutPassword_ShouldNotUpdatePassword() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFullName("Old Name");
        existingUser.setEmail("old@hm.edu");
        existingUser.setPassword("$2a$10$oldEncodedPassword");
        existingUser.setRole(User.Role.USER);

        User updatedUser = new User();
        updatedUser.setFullName("Updated Name");
        updatedUser.setEmail("updated@hm.edu");
        updatedUser.setPassword(""); // Empty password should not trigger update
        updatedUser.setRole(User.Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.updateUser(1L, updatedUser);

        // Then
        assertEquals("Updated Name", result.getFullName());
        assertEquals("updated@hm.edu", result.getEmail());
        assertEquals("$2a$10$oldEncodedPassword", result.getPassword()); // Password remains unchanged
        assertEquals(User.Role.ADMIN, result.getRole());

        verify(userRepository).findById(1L);
        verifyNoInteractions(passwordEncoder);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_WithNullPassword_ShouldNotUpdatePassword() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFullName("Old Name");
        existingUser.setEmail("old@hm.edu");
        existingUser.setPassword("$2a$10$oldEncodedPassword");
        existingUser.setRole(User.Role.USER);

        User updatedUser = new User();
        updatedUser.setFullName("Updated Name");
        updatedUser.setEmail("updated@hm.edu");
        updatedUser.setPassword(null); // Null password should not trigger update
        updatedUser.setRole(User.Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.updateUser(1L, updatedUser);

        // Then
        assertEquals("$2a$10$oldEncodedPassword", result.getPassword()); // Password remains unchanged
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateUserName_ShouldOnlyUpdateName() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFullName("Old Name");
        existingUser.setEmail("user@hm.edu");
        existingUser.setPassword("$2a$10$encodedPassword");
        existingUser.setRole(User.Role.USER);

        String newName = "New Name";

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.updateUserName(1L, newName);

        // Then
        assertEquals(newName, result.getFullName());
        assertEquals("user@hm.edu", result.getEmail()); // Should remain unchanged
        assertEquals("$2a$10$encodedPassword", result.getPassword()); // Should remain unchanged
        assertEquals(User.Role.USER, result.getRole()); // Should remain unchanged

        verify(userRepository).findById(1L);
        verify(userRepository).save(existingUser);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateUserPassword_WithCorrectCurrentPassword_ShouldUpdatePassword() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFullName("Test User");
        existingUser.setEmail("user@hm.edu");
        existingUser.setPassword("$2a$10$currentEncodedPassword");
        existingUser.setRole(User.Role.USER);

        String currentPassword = "currentPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(currentPassword, "$2a$10$currentEncodedPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        userService.updateUserPassword(1L, currentPassword, newPassword);

        // Then
        assertEquals("$2a$10$newEncodedPassword", existingUser.getPassword());

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(currentPassword, "$2a$10$currentEncodedPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUserPassword_WithIncorrectCurrentPassword_ShouldThrowException() {
        // Given
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFullName("Test User");
        existingUser.setEmail("user@hm.edu");
        existingUser.setPassword("$2a$10$currentEncodedPassword");
        existingUser.setRole(User.Role.USER);

        String wrongPassword = "wrongPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(wrongPassword, "$2a$10$currentEncodedPassword")).thenReturn(false);

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserPassword(1L, wrongPassword, newPassword);
        });

        assertEquals("Current password is incorrect", exception.getMessage());

        verify(userRepository).findById(1L);
        verify(passwordEncoder).matches(wrongPassword, "$2a$10$currentEncodedPassword");
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_ShouldCallRepositoryDeleteById() {
        // Given
        Long userId = 1L;
        doNothing().when(userRepository).deleteById(userId);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).deleteById(userId);
    }
}