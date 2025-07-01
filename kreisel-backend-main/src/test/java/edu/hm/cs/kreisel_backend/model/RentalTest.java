package edu.hm.cs.kreisel_backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class RentalTest {

    private Rental rental;

    @Mock
    private User user;

    @Mock
    private Item item;

    private LocalDate today;
    private LocalDate yesterday;
    private LocalDate tomorrow;
    private LocalDate nextWeek;

    @BeforeEach
    void setUp() {
        rental = new Rental();

        today = LocalDate.now();
        yesterday = today.minusDays(1);
        tomorrow = today.plusDays(1);
        nextWeek = today.plusDays(7);

        rental.setId(1L);
        rental.setUser(user);
        rental.setItem(item);
        rental.setRentalDate(yesterday);
        rental.setEndDate(nextWeek);
        rental.setReturnDate(null);
        rental.setExtended(false);
    }

    @Test
    void testBasicProperties() {
        assertEquals(1L, rental.getId());
        assertSame(user, rental.getUser());
        assertSame(item, rental.getItem());
        assertEquals(yesterday, rental.getRentalDate());
        assertEquals(nextWeek, rental.getEndDate());
        assertNull(rental.getReturnDate());
        assertFalse(rental.isExtended());
    }

    @Test
    void testStatus_Active() {
        // Eine aktive Ausleihe: Enddatum liegt in der Zukunft, nicht zurückgegeben
        assertEquals("ACTIVE", rental.getStatus());
    }

    @Test
    void testStatus_Returned() {
        // Eine zurückgegebene Ausleihe: returnDate ist gesetzt
        rental.setReturnDate(today);
        assertEquals("RETURNED", rental.getStatus());
    }

    @Test
    void testStatus_Overdue() {
        // Eine überfällige Ausleihe: Enddatum ist in der Vergangenheit, nicht zurückgegeben
        rental.setEndDate(yesterday);
        rental.setReturnDate(null);

        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(today);
            assertEquals("OVERDUE", rental.getStatus());
        }
    }

    @Test
    void testStatus_OverdueButReturned() {
        // Eine überfällige aber zurückgegebene Ausleihe: Status sollte RETURNED sein
        rental.setEndDate(yesterday);
        rental.setReturnDate(today);

        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(today);
            assertEquals("RETURNED", rental.getStatus());
        }
    }

    @Test
    void testExtendedFlag() {
        // Testen der Verlängerungsfunktion
        assertFalse(rental.isExtended());
        rental.setExtended(true);
        assertTrue(rental.isExtended());
    }

    @Test
    void testEndDateInPast_NotReturned() {
        // Eine Ausleihe mit Enddatum in der Vergangenheit und nicht zurückgegeben sollte überfällig sein
        LocalDate pastDate = today.minusDays(5);
        rental.setEndDate(pastDate);

        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(today);
            assertEquals("OVERDUE", rental.getStatus());
        }
    }

    @Test
    void testEndDateToday_NotReturned() {
        // Eine Ausleihe mit Enddatum heute und nicht zurückgegeben sollte aktiv sein (fällig heute)
        rental.setEndDate(today);

        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(today);
            assertEquals("ACTIVE", rental.getStatus());
        }
    }

    @Test
    void testRentedTodayEndDateTomorrow() {
        // Ausleihe von heute bis morgen sollte aktiv sein
        rental.setRentalDate(today);
        rental.setEndDate(tomorrow);

        try (MockedStatic<LocalDate> mockedLocalDate = Mockito.mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(today);
            assertEquals("ACTIVE", rental.getStatus());
        }
    }

    @Test
    void testReturnedBeforeEndDate() {
        // Ausleihe wurde vor dem Enddatum zurückgegeben
        rental.setRentalDate(yesterday);
        rental.setEndDate(tomorrow);
        rental.setReturnDate(today);

        assertEquals("RETURNED", rental.getStatus());
    }
}