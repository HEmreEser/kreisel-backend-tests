package edu.hm.cs.kreisel_backend.service;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.model.Rental;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.repository.ItemRepository;
import edu.hm.cs.kreisel_backend.repository.RentalRepository;
import edu.hm.cs.kreisel_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RentalService {

    private static final int MAX_ACTIVE_RENTALS = 5;
    private static final int MAX_RENTAL_DAYS = 90; // 3 Monate maximal
    private static final int EXTENSION_DAYS = 30; // Verlängerung um 30 Tage

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public List<Rental> getRentalsByUser(Long userId) {
        return rentalRepository.findByUserId(userId);
    }

    public List<Rental> getActiveRentalsByUser(Long userId) {
        return rentalRepository.findByUserIdAndReturnDateIsNull(userId);
    }

    public List<Rental> getHistoricalRentalsByUser(Long userId) {
        return rentalRepository.findByUserIdAndReturnDateIsNotNull(userId);
    }

    public Optional<Rental> getActiveRentalForItem(Long itemId) {
        return rentalRepository.findByItemIdAndReturnDateIsNull(itemId);
    }

    public Rental getRentalById(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElse(null);
    }

    public Rental rentItem(Long userId, Long itemId, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        validateRentalRequest(userId, item, endDate);

        Rental rental = new Rental();
        rental.setUser(user);
        rental.setItem(item);
        rental.setRentalDate(LocalDate.now());
        rental.setEndDate(validateAndSetEndDate(endDate));
        rental.setReturnDate(null);
        rental.setExtended(false);

        item.setAvailable(false);
        itemRepository.save(item);

        return rentalRepository.save(rental);
    }


    private void validateRentalRequest(Long userId, Item item, LocalDate endDate) {
        if (!item.isAvailable()) {
            throw new RuntimeException("Item ist nicht verfügbar");
        }

        List<Rental> activeRentals = getActiveRentalsByUser(userId);
        if (activeRentals.size() >= MAX_ACTIVE_RENTALS) {
            throw new RuntimeException("Maximale Anzahl aktiver Ausleihen (5) erreicht");
        }

        if (getActiveRentalForItem(item.getId()).isPresent()) {
            throw new RuntimeException("Item ist bereits ausgeliehen");
        }

        if (endDate == null) {
            throw new RuntimeException("Enddatum ist erforderlich");
        }
    }


    private LocalDate validateAndSetEndDate(LocalDate requestedEndDate) {
        LocalDate today = LocalDate.now();
        LocalDate maxEndDate = today.plusDays(MAX_RENTAL_DAYS);

        if (requestedEndDate.isBefore(today)) {
            throw new RuntimeException("Enddatum darf nicht in der Vergangenheit liegen");
        }

        if (requestedEndDate.isAfter(maxEndDate)) {
            throw new RuntimeException("Enddatum darf maximal " + MAX_RENTAL_DAYS + " Tage in der Zukunft liegen");
        }

        if (requestedEndDate.isEqual(today)) {
            throw new RuntimeException("Ausleihdauer muss mindestens 1 Tag betragen");
        }

        return requestedEndDate;
    }


    public Rental extendRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        if (rental.getReturnDate() != null) {
            throw new RuntimeException("Rental ist bereits zurückgegeben");
        }

        if (rental.isExtended()) {
            throw new RuntimeException("Verlängerung bereits genutzt");
        }

        // Neues Enddatum berechnen (um 30 Tage verlängern)
        LocalDate newEndDate = rental.getEndDate().plusDays(EXTENSION_DAYS);
        LocalDate maxAllowedDate = rental.getRentalDate().plusDays(MAX_RENTAL_DAYS + EXTENSION_DAYS);

        // Prüfen ob die Verlängerung das absolute Maximum überschreitet
        if (newEndDate.isAfter(maxAllowedDate)) {
            throw new RuntimeException("Verlängerung würde die maximale Ausleihdauer überschreiten");
        }

        rental.setEndDate(newEndDate);
        rental.setExtended(true);

        return rentalRepository.save(rental);
    }

    public Rental returnRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        if (rental.getReturnDate() != null) {
            throw new RuntimeException("Rental ist bereits zurückgegeben");
        }

        rental.setReturnDate(LocalDate.now());

        // Item als verfügbar markieren
        Item item = rental.getItem();
        item.setAvailable(true);
        itemRepository.save(item);

        return rentalRepository.save(rental);
    }

}
