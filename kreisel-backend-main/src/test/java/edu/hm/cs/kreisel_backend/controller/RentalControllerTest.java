package edu.hm.cs.kreisel_backend.controller;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.model.Rental;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.security.SecurityUtils;
import edu.hm.cs.kreisel_backend.service.RentalService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RentalControllerTest {

    @Mock
    private RentalService rentalService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private RentalController rentalController;

    private User regularUser;
    private User adminUser;
    private Item testItem;
    private Rental testRental;
    private Rental testRentalOfOtherUser;
    private List<Rental> rentalList;
    private List<Rental> activeRentals;
    private List<Rental> historicalRentals;
    private Map<String, String> rentItemRequest;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        // Setup users
        regularUser = new User();
        regularUser.setId(1L);
        regularUser.setFullName("Regular User");
        regularUser.setEmail("user@hm.edu");
        regularUser.setRole(User.Role.USER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setFullName("Admin User");
        adminUser.setEmail("admin@hm.edu");
        adminUser.setRole(User.Role.ADMIN);

        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setFullName("Other User");
        otherUser.setEmail("other@hm.edu");
        otherUser.setRole(User.Role.USER);

        // Setup item
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");

        // Setup dates
        LocalDate now = LocalDate.now();
        endDate = now.plusDays(7);

        // Setup rentals
        testRental = new Rental();
        testRental.setId(1L);
        testRental.setUser(regularUser);
        testRental.setItem(testItem);
        testRental.setRentalDate(now);
        testRental.setEndDate(endDate);
        testRental.setExtended(false);

        testRentalOfOtherUser = new Rental();
        testRentalOfOtherUser.setId(2L);
        testRentalOfOtherUser.setUser(otherUser);
        testRentalOfOtherUser.setItem(testItem);
        testRentalOfOtherUser.setRentalDate(now);
        testRentalOfOtherUser.setEndDate(endDate);
        testRentalOfOtherUser.setExtended(false);

        Rental completedRental = new Rental();
        completedRental.setId(3L);
        completedRental.setUser(regularUser);
        completedRental.setItem(testItem);
        completedRental.setRentalDate(now.minusDays(14));
        completedRental.setEndDate(now.minusDays(7));
        completedRental.setReturnDate(now.minusDays(7));
        completedRental.setExtended(false);

        // Setup rental lists
        rentalList = Arrays.asList(testRental, testRentalOfOtherUser, completedRental);
        activeRentals = Arrays.asList(testRental);
        historicalRentals = Arrays.asList(completedRental);

        // Setup request
        rentItemRequest = new HashMap<>();
        rentItemRequest.put("itemId", "1");
        rentItemRequest.put("endDate", endDate.toString());
    }

    @Test
    void getAllRentals_ShouldReturnAllRentals() {
        // Given
        when(rentalService.getAllRentals()).thenReturn(rentalList);

        // When
        ResponseEntity<List<Rental>> response = rentalController.getAllRentals();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(rentalList, response.getBody());
        verify(rentalService).getAllRentals();
    }

    @Test
    void getCurrentUserRentals_WhenAuthenticated_ShouldReturnUserRentals() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);
        when(rentalService.getRentalsByUser(regularUser.getId())).thenReturn(Arrays.asList(testRental, testRental));

        // When
        ResponseEntity<List<Rental>> response = rentalController.getCurrentUserRentals();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getRentalsByUser(regularUser.getId());
    }

    @Test
    void getCurrentUserRentals_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<List<Rental>> response = rentalController.getCurrentUserRentals();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(securityUtils).getCurrentUser();
    }

    @Test
    void getCurrentUserActiveRentals_WhenAuthenticated_ShouldReturnActiveRentals() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);
        when(rentalService.getActiveRentalsByUser(regularUser.getId())).thenReturn(activeRentals);

        // When
        ResponseEntity<List<Rental>> response = rentalController.getCurrentUserActiveRentals();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(activeRentals, response.getBody());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getActiveRentalsByUser(regularUser.getId());
    }

    @Test
    void getCurrentUserActiveRentals_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<List<Rental>> response = rentalController.getCurrentUserActiveRentals();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(securityUtils).getCurrentUser();
    }

    @Test
    void getCurrentUserHistoricalRentals_WhenAuthenticated_ShouldReturnHistoricalRentals() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);
        when(rentalService.getHistoricalRentalsByUser(regularUser.getId())).thenReturn(historicalRentals);

        // When
        ResponseEntity<List<Rental>> response = rentalController.getCurrentUserHistoricalRentals();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(historicalRentals, response.getBody());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getHistoricalRentalsByUser(regularUser.getId());
    }

    @Test
    void getCurrentUserHistoricalRentals_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<List<Rental>> response = rentalController.getCurrentUserHistoricalRentals();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(securityUtils).getCurrentUser();
    }

    @Test
    void rentItem_WhenAuthenticated_ShouldRentItem() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);
        when(rentalService.rentItem(regularUser.getId(), 1L, endDate)).thenReturn(testRental);

        // When
        ResponseEntity<Rental> response = rentalController.rentItem(rentItemRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testRental, response.getBody());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).rentItem(regularUser.getId(), 1L, endDate);
    }

    @Test
    void rentItem_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<Rental> response = rentalController.rentItem(rentItemRequest);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
        verify(securityUtils).getCurrentUser();
    }

    // Tests for backward compatibility endpoints

    @Test
    void getRentalsByUser_ShouldReturnUserRentals() {
        // Given
        Long userId = 1L;
        when(rentalService.getRentalsByUser(userId)).thenReturn(Arrays.asList(testRental, testRental));

        // When
        ResponseEntity<List<Rental>> response = rentalController.getRentalsByUser(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        verify(rentalService).getRentalsByUser(userId);
    }

    @Test
    void getActiveRentalsByUser_ShouldReturnActiveRentals() {
        // Given
        Long userId = 1L;
        when(rentalService.getActiveRentalsByUser(userId)).thenReturn(activeRentals);

        // When
        ResponseEntity<List<Rental>> response = rentalController.getActiveRentalsByUser(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(activeRentals, response.getBody());
        verify(rentalService).getActiveRentalsByUser(userId);
    }

    @Test
    void getHistoricalRentalsByUser_ShouldReturnHistoricalRentals() {
        // Given
        Long userId = 1L;
        when(rentalService.getHistoricalRentalsByUser(userId)).thenReturn(historicalRentals);

        // When
        ResponseEntity<List<Rental>> response = rentalController.getHistoricalRentalsByUser(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(historicalRentals, response.getBody());
        verify(rentalService).getHistoricalRentalsByUser(userId);
    }

    @Test
    void rentItemForSpecificUser_ShouldRentItem() {
        // Given
        Long userId = 1L;
        when(rentalService.rentItem(userId, 1L, endDate)).thenReturn(testRental);

        // When
        ResponseEntity<Rental> response = rentalController.rentItem(userId, rentItemRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testRental, response.getBody());
        verify(rentalService).rentItem(userId, 1L, endDate);
    }

    @Test
    void extendRental_AsOwnerOfRental_ShouldExtendRental() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);
        when(rentalService.getRentalById(1L)).thenReturn(testRental);
        when(rentalService.extendRental(1L)).thenReturn(testRental);

        // When
        ResponseEntity<Rental> response = rentalController.extendRental(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testRental, response.getBody());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getRentalById(1L);
        verify(rentalService).extendRental(1L);
    }

    @Test
    void extendRental_AsAdmin_ShouldExtendRental() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(rentalService.getRentalById(2L)).thenReturn(testRentalOfOtherUser);
        when(rentalService.extendRental(2L)).thenReturn(testRentalOfOtherUser);

        // When
        ResponseEntity<Rental> response = rentalController.extendRental(2L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testRentalOfOtherUser, response.getBody());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getRentalById(2L);
        verify(rentalService).extendRental(2L);
    }

    @Test
    void extendRental_ForNonExistentRental_ShouldReturnNotFound() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);
        when(rentalService.getRentalById(999L)).thenReturn(null);

        // When
        ResponseEntity<Rental> response = rentalController.extendRental(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getRentalById(999L);
    }

    @Test
    void extendRental_ForOtherUserRental_ShouldReturnForbidden() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);
        when(rentalService.getRentalById(2L)).thenReturn(testRentalOfOtherUser);

        // When
        ResponseEntity<Rental> response = rentalController.extendRental(2L);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getRentalById(2L);
    }

    @Test
    void extendRental_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<Rental> response = rentalController.extendRental(1L);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
    }

    @Test
    void returnRental_AsOwnerOfRental_ShouldReturnRental() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);
        when(rentalService.getRentalById(1L)).thenReturn(testRental);
        when(rentalService.returnRental(1L)).thenReturn(testRental);

        // When
        ResponseEntity<Rental> response = rentalController.returnRental(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testRental, response.getBody());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getRentalById(1L);
        verify(rentalService).returnRental(1L);
    }

    @Test
    void returnRental_AsAdmin_ShouldReturnRental() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(adminUser);
        when(rentalService.getRentalById(2L)).thenReturn(testRentalOfOtherUser);
        when(rentalService.returnRental(2L)).thenReturn(testRentalOfOtherUser);

        // When
        ResponseEntity<Rental> response = rentalController.returnRental(2L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testRentalOfOtherUser, response.getBody());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getRentalById(2L);
        verify(rentalService).returnRental(2L);
    }

    @Test
    void returnRental_ForNonExistentRental_ShouldReturnNotFound() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);
        when(rentalService.getRentalById(999L)).thenReturn(null);

        // When
        ResponseEntity<Rental> response = rentalController.returnRental(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getRentalById(999L);
    }

    @Test
    void returnRental_ForOtherUserRental_ShouldReturnForbidden() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(regularUser);
        when(rentalService.getRentalById(2L)).thenReturn(testRentalOfOtherUser);

        // When
        ResponseEntity<Rental> response = rentalController.returnRental(2L);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
        verify(rentalService).getRentalById(2L);
    }

    @Test
    void returnRental_WhenNotAuthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<Rental> response = rentalController.returnRental(1L);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(securityUtils).getCurrentUser();
    }
}