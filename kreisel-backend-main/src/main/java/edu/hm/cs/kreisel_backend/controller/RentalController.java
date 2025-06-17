package edu.hm.cs.kreisel_backend.controller;

import edu.hm.cs.kreisel_backend.model.Rental;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.security.SecurityUtils;
import edu.hm.cs.kreisel_backend.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<Rental>> getAllRentals() {
        return ResponseEntity.ok(rentalService.getAllRentals());
    }

    @GetMapping("/user")
    public ResponseEntity<List<Rental>> getCurrentUserRentals() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(rentalService.getRentalsByUser(currentUser.getId()));
    }

    @GetMapping("/user/active")
    public ResponseEntity<List<Rental>> getCurrentUserActiveRentals() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(rentalService.getActiveRentalsByUser(currentUser.getId()));
    }

    @GetMapping("/user/history")
    public ResponseEntity<List<Rental>> getCurrentUserHistoricalRentals() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(rentalService.getHistoricalRentalsByUser(currentUser.getId()));
    }

    @PostMapping("/rent")
    public ResponseEntity<Rental> rentItem(@RequestBody Map<String, String> request) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        Long itemId = Long.valueOf(request.get("itemId"));
        LocalDate endDate = LocalDate.parse(request.get("endDate"));
        return ResponseEntity.ok(rentalService.rentItem(currentUser.getId(), itemId, endDate));
    }

    // For backward compatibility
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Rental>> getRentalsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(rentalService.getRentalsByUser(userId));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Rental>> getActiveRentalsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(rentalService.getActiveRentalsByUser(userId));
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<Rental>> getHistoricalRentalsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(rentalService.getHistoricalRentalsByUser(userId));
    }

    @PostMapping("/user/{userId}/rent")
    public ResponseEntity<Rental> rentItem(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        Long itemId = Long.valueOf(request.get("itemId"));
        LocalDate endDate = LocalDate.parse(request.get("endDate"));
        return ResponseEntity.ok(rentalService.rentItem(userId, itemId, endDate));
    }

    @PostMapping("/{rentalId}/extend")
    public ResponseEntity<Rental> extendRental(@PathVariable Long rentalId) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // Check if the rental belongs to the current user or if the user is an admin
        Rental rental = rentalService.getRentalById(rentalId);
        if (rental == null) {
            return ResponseEntity.notFound().build();
        }

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !rental.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(rentalService.extendRental(rentalId));
    }

    @PostMapping("/{rentalId}/return")
    public ResponseEntity<Rental> returnRental(@PathVariable Long rentalId) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // Check if the rental belongs to the current user or if the user is an admin
        Rental rental = rentalService.getRentalById(rentalId);
        if (rental == null) {
            return ResponseEntity.notFound().build();
        }

        if (!currentUser.getRole().equals(User.Role.ADMIN) && !rental.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(rentalService.returnRental(rentalId));
    }
}
