package edu.hm.cs.kreisel_backend.service;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.model.Rental;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.repository.ItemRepository;
import edu.hm.cs.kreisel_backend.repository.RentalRepository;
import edu.hm.cs.kreisel_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private RentalService rentalService;

    private User testUser;
    private Item availableItem;
    private Item unavailableItem;
    private Rental activeRental;
    private Rental completedRental;
    private List<Rental> activeRentals;
    private List<Rental> historicalRentals;
    private List<Rental> allRentals;
    private LocalDate today;
    private LocalDate futureDate;
    private LocalDate pastDate;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();
        futureDate = today.plusDays(14);
        pastDate = today.minusDays(7);

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@hm.edu");

        // Setup available item
        availableItem = new Item();
        availableItem.setId(1L);
        availableItem.setName("Available Item");
        availableItem.setAvailable(true);

        // Setup unavailable item
        unavailableItem = new Item();
        unavailableItem.setId(2L);
        unavailableItem.setName("Unavailable Item");
        unavailableItem.setAvailable(false);

        // Setup active rental
        activeRental = new Rental();
        activeRental.setId(1L);
        activeRental.setUser(testUser);
        activeRental.setItem(unavailableItem);
        activeRental.setRentalDate(pastDate);
        activeRental.setEndDate(futureDate);
        activeRental.setReturnDate(null);
        activeRental.setExtended(false);

        // Setup completed rental
        completedRental = new Rental();
        completedRental.setId(2L);
        completedRental.setUser(testUser);
        completedRental.setItem(availableItem);
        completedRental.setRentalDate(pastDate.minusDays(14));
        completedRental.setEndDate(pastDate);
        completedRental.setReturnDate(pastDate);
        completedRental.setExtended(false);

        // Setup rental lists
        activeRentals = Collections.singletonList(activeRental);
        historicalRentals = Collections.singletonList(completedRental);
        allRentals = Arrays.asList(activeRental, completedRental);
    }

    @Test
    void getAllRentals_ShouldReturnAllRentals() {
        // Given
        when(rentalRepository.findAll()).thenReturn(allRentals);

        // When
        List<Rental> result = rentalService.getAllRentals();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(allRentals));
        verify(rentalRepository).findAll();
    }

    @Test
    void getRentalsByUser_ShouldReturnUserRentals() {
        // Given
        when(rentalRepository.findByUserId(testUser.getId())).thenReturn(allRentals);

        // When
        List<Rental> result = rentalService.getRentalsByUser(testUser.getId());

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(allRentals));
        verify(rentalRepository).findByUserId(testUser.getId());
    }

    @Test
    void getActiveRentalsByUser_ShouldReturnActiveRentals() {
        // Given
        when(rentalRepository.findByUserIdAndReturnDateIsNull(testUser.getId())).thenReturn(activeRentals);

        // When
        List<Rental> result = rentalService.getActiveRentalsByUser(testUser.getId());

        // Then
        assertEquals(1, result.size());
        assertEquals(activeRental, result.get(0));
        verify(rentalRepository).findByUserIdAndReturnDateIsNull(testUser.getId());
    }

    @Test
    void getHistoricalRentalsByUser_ShouldReturnHistoricalRentals() {
        // Given
        when(rentalRepository.findByUserIdAndReturnDateIsNotNull(testUser.getId())).thenReturn(historicalRentals);

        // When
        List<Rental> result = rentalService.getHistoricalRentalsByUser(testUser.getId());

        // Then
        assertEquals(1, result.size());
        assertEquals(completedRental, result.get(0));
        verify(rentalRepository).findByUserIdAndReturnDateIsNotNull(testUser.getId());
    }

    @Test
    void getActiveRentalForItem_WhenExists_ShouldReturnRental() {
        // Given
        when(rentalRepository.findByItemIdAndReturnDateIsNull(unavailableItem.getId())).thenReturn(Optional.of(activeRental));

        // When
        Optional<Rental> result = rentalService.getActiveRentalForItem(unavailableItem.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(activeRental, result.get());
        verify(rentalRepository).findByItemIdAndReturnDateIsNull(unavailableItem.getId());
    }

    @Test
    void getActiveRentalForItem_WhenNotExists_ShouldReturnEmpty() {
        // Given
        when(rentalRepository.findByItemIdAndReturnDateIsNull(availableItem.getId())).thenReturn(Optional.empty());

        // When
        Optional<Rental> result = rentalService.getActiveRentalForItem(availableItem.getId());

        // Then
        assertFalse(result.isPresent());
        verify(rentalRepository).findByItemIdAndReturnDateIsNull(availableItem.getId());
    }

    @Test
    void getRentalById_WhenExists_ShouldReturnRental() {
        // Given
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(activeRental));

        // When
        Rental result = rentalService.getRentalById(1L);

        // Then
        assertNotNull(result);
        assertEquals(activeRental, result);
        verify(rentalRepository).findById(1L);
    }

    @Test
    void getRentalById_WhenNotExists_ShouldReturnNull() {
        // Given
        when(rentalRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Rental result = rentalService.getRentalById(99L);

        // Then
        assertNull(result);
        verify(rentalRepository).findById(99L);
    }

    @Test
    void rentItem_WithValidData_ShouldCreateRental() {
        // Given
        LocalDate validEndDate = today.plusDays(7);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(availableItem.getId())).thenReturn(Optional.of(availableItem));
        when(rentalRepository.findByUserIdAndReturnDateIsNull(testUser.getId())).thenReturn(Collections.emptyList());
        when(rentalRepository.findByItemIdAndReturnDateIsNull(availableItem.getId())).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> {
            Rental savedRental = invocation.getArgument(0);
            savedRental.setId(3L);
            return savedRental;
        });

        // When
        Rental result = rentalService.rentItem(testUser.getId(), availableItem.getId(), validEndDate);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(availableItem, result.getItem());
        assertEquals(today, result.getRentalDate());
        assertEquals(validEndDate, result.getEndDate());
        assertNull(result.getReturnDate());
        assertFalse(result.isExtended());

        // Verify item availability was updated
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        Item savedItem = itemCaptor.getValue();
        assertFalse(savedItem.isAvailable());

        // Verify rental was saved
        verify(rentalRepository).save(any(Rental.class));
    }

    @Test
    void rentItem_WithUnavailableItem_ShouldThrowException() {
        // Given
        LocalDate validEndDate = today.plusDays(7);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(unavailableItem.getId())).thenReturn(Optional.of(unavailableItem));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.rentItem(testUser.getId(), unavailableItem.getId(), validEndDate);
        });

        assertEquals("Item ist nicht verfügbar", exception.getMessage());
        verify(userRepository).findById(testUser.getId());
        verify(itemRepository).findById(unavailableItem.getId());
        verifyNoMoreInteractions(rentalRepository, itemRepository);
    }

    @Test
    void rentItem_WithTooManyActiveRentals_ShouldThrowException() {
        // Given
        LocalDate validEndDate = today.plusDays(7);

        // Create 5 active rentals (max limit)
        List<Rental> maxActiveRentals = Arrays.asList(
                new Rental(), new Rental(), new Rental(), new Rental(), new Rental()
        );

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(availableItem.getId())).thenReturn(Optional.of(availableItem));
        when(rentalRepository.findByUserIdAndReturnDateIsNull(testUser.getId())).thenReturn(maxActiveRentals);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.rentItem(testUser.getId(), availableItem.getId(), validEndDate);
        });

        assertEquals("Maximale Anzahl aktiver Ausleihen (5) erreicht", exception.getMessage());
        verify(userRepository).findById(testUser.getId());
        verify(itemRepository).findById(availableItem.getId());
        verify(rentalRepository).findByUserIdAndReturnDateIsNull(testUser.getId());
        verifyNoMoreInteractions(rentalRepository, itemRepository);
    }

    @Test
    void rentItem_WithAlreadyRentedItem_ShouldThrowException() {
        // Given
        LocalDate validEndDate = today.plusDays(7);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(availableItem.getId())).thenReturn(Optional.of(availableItem));
        when(rentalRepository.findByUserIdAndReturnDateIsNull(testUser.getId())).thenReturn(Collections.emptyList());
        when(rentalRepository.findByItemIdAndReturnDateIsNull(availableItem.getId())).thenReturn(Optional.of(new Rental()));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.rentItem(testUser.getId(), availableItem.getId(), validEndDate);
        });

        assertEquals("Item ist bereits ausgeliehen", exception.getMessage());
        verify(userRepository).findById(testUser.getId());
        verify(itemRepository).findById(availableItem.getId());
        verify(rentalRepository).findByUserIdAndReturnDateIsNull(testUser.getId());
        verify(rentalRepository).findByItemIdAndReturnDateIsNull(availableItem.getId());
        verifyNoMoreInteractions(rentalRepository, itemRepository);
    }

    @Test
    void rentItem_WithNullEndDate_ShouldThrowException() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(availableItem.getId())).thenReturn(Optional.of(availableItem));
        when(rentalRepository.findByUserIdAndReturnDateIsNull(testUser.getId())).thenReturn(Collections.emptyList());
        when(rentalRepository.findByItemIdAndReturnDateIsNull(availableItem.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.rentItem(testUser.getId(), availableItem.getId(), null);
        });

        assertEquals("Enddatum ist erforderlich", exception.getMessage());
        verify(userRepository).findById(testUser.getId());
        verify(itemRepository).findById(availableItem.getId());
        verify(rentalRepository).findByUserIdAndReturnDateIsNull(testUser.getId());
        verify(rentalRepository).findByItemIdAndReturnDateIsNull(availableItem.getId());
        verifyNoMoreInteractions(rentalRepository, itemRepository);
    }

    @Test
    void rentItem_WithPastEndDate_ShouldThrowException() {
        // Given
        LocalDate pastEndDate = today.minusDays(1);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(availableItem.getId())).thenReturn(Optional.of(availableItem));
        when(rentalRepository.findByUserIdAndReturnDateIsNull(testUser.getId())).thenReturn(Collections.emptyList());
        when(rentalRepository.findByItemIdAndReturnDateIsNull(availableItem.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.rentItem(testUser.getId(), availableItem.getId(), pastEndDate);
        });

        assertEquals("Enddatum darf nicht in der Vergangenheit liegen", exception.getMessage());
    }

    @Test
    void rentItem_WithTooDistantEndDate_ShouldThrowException() {
        // Given
        LocalDate tooDistantEndDate = today.plusDays(91); // MAX_RENTAL_DAYS is 90

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(availableItem.getId())).thenReturn(Optional.of(availableItem));
        when(rentalRepository.findByUserIdAndReturnDateIsNull(testUser.getId())).thenReturn(Collections.emptyList());
        when(rentalRepository.findByItemIdAndReturnDateIsNull(availableItem.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.rentItem(testUser.getId(), availableItem.getId(), tooDistantEndDate);
        });

        assertEquals("Enddatum darf maximal 90 Tage in der Zukunft liegen", exception.getMessage());
    }

    @Test
    void rentItem_WithSameDayEndDate_ShouldThrowException() {
        // Given
        LocalDate sameDayEndDate = today; // Same day

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(availableItem.getId())).thenReturn(Optional.of(availableItem));
        when(rentalRepository.findByUserIdAndReturnDateIsNull(testUser.getId())).thenReturn(Collections.emptyList());
        when(rentalRepository.findByItemIdAndReturnDateIsNull(availableItem.getId())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.rentItem(testUser.getId(), availableItem.getId(), sameDayEndDate);
        });

        assertEquals("Ausleihdauer muss mindestens 1 Tag betragen", exception.getMessage());
    }

    @Test
    void rentItem_WithNonExistentUser_ShouldThrowException() {
        // Given
        LocalDate validEndDate = today.plusDays(7);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.rentItem(99L, availableItem.getId(), validEndDate);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(99L);
        verifyNoInteractions(itemRepository, rentalRepository);
    }

    @Test
    void rentItem_WithNonExistentItem_ShouldThrowException() {
        // Given
        LocalDate validEndDate = today.plusDays(7);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.rentItem(testUser.getId(), 99L, validEndDate);
        });

        assertEquals("Item not found", exception.getMessage());
        verify(userRepository).findById(testUser.getId());
        verify(itemRepository).findById(99L);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    void extendRental_WhenValid_ShouldExtendRental() {
        // Given
        when(rentalRepository.findById(activeRental.getId())).thenReturn(Optional.of(activeRental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Rental result = rentalService.extendRental(activeRental.getId());

        // Then
        assertTrue(result.isExtended());
        assertEquals(futureDate.plusDays(30), result.getEndDate()); // 30 days extension

        verify(rentalRepository).findById(activeRental.getId());
        verify(rentalRepository).save(activeRental);
    }

    @Test
    void extendRental_WhenAlreadyExtended_ShouldThrowException() {
        // Given
        activeRental.setExtended(true);
        when(rentalRepository.findById(activeRental.getId())).thenReturn(Optional.of(activeRental));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.extendRental(activeRental.getId());
        });

        assertEquals("Verlängerung bereits genutzt", exception.getMessage());
        verify(rentalRepository).findById(activeRental.getId());
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    void extendRental_WhenAlreadyReturned_ShouldThrowException() {
        // Given
        when(rentalRepository.findById(completedRental.getId())).thenReturn(Optional.of(completedRental));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.extendRental(completedRental.getId());
        });

        assertEquals("Rental ist bereits zurückgegeben", exception.getMessage());
        verify(rentalRepository).findById(completedRental.getId());
        verifyNoMoreInteractions(rentalRepository);
    }


    @Test
    void extendRental_WhenRentalNotFound_ShouldThrowException() {
        // Given
        when(rentalRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.extendRental(99L);
        });

        assertEquals("Rental not found", exception.getMessage());
        verify(rentalRepository).findById(99L);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    void returnRental_WhenActive_ShouldMarkAsReturned() {
        // Given
        when(rentalRepository.findById(activeRental.getId())).thenReturn(Optional.of(activeRental));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Rental result = rentalService.returnRental(activeRental.getId());

        // Then
        assertEquals(today, result.getReturnDate());

        // Verify item availability was updated
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        Item savedItem = itemCaptor.getValue();
        assertTrue(savedItem.isAvailable());

        verify(rentalRepository).findById(activeRental.getId());
        verify(rentalRepository).save(activeRental);
    }

    @Test
    void returnRental_WhenAlreadyReturned_ShouldThrowException() {
        // Given
        when(rentalRepository.findById(completedRental.getId())).thenReturn(Optional.of(completedRental));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.returnRental(completedRental.getId());
        });

        assertEquals("Rental ist bereits zurückgegeben", exception.getMessage());
        verify(rentalRepository).findById(completedRental.getId());
        verifyNoInteractions(itemRepository);
        verifyNoMoreInteractions(rentalRepository);
    }

    @Test
    void returnRental_WhenRentalNotFound_ShouldThrowException() {
        // Given
        when(rentalRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            rentalService.returnRental(99L);
        });

        assertEquals("Rental not found", exception.getMessage());
        verify(rentalRepository).findById(99L);
        verifyNoInteractions(itemRepository);
        verifyNoMoreInteractions(rentalRepository);
    }
}