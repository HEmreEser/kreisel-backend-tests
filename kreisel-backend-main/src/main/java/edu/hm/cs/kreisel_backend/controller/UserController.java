package edu.hm.cs.kreisel_backend.controller;

import edu.hm.cs.kreisel_backend.model.Rental;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.security.SecurityUtils;
import edu.hm.cs.kreisel_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    // Nur der admin soll diese Methode haben um alle user zu suchen
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Wieder nur der admin soll diese Methode haben um nach den Usern zu schauen
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // Nur der admin soll diese Methode haben um nach den Usern zu schauen
    @GetMapping("/email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    // Nur der admin soll diese Methode haben um nach den rentals der User zu schauen, der User kann seine eigenen rentals sehen
    @GetMapping("/{id}/rentals")
    public List<Rental> getUserRentals(@PathVariable Long id) {
        return userService.getRentalsByUserId(id);
    }

    // Nur admin
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // Nur admin
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    // Nur admin
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // User can edit their own account
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(currentUser);
    }

    // User can edit their own account
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@RequestBody User user) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        // Ensure user can't change their role
        user.setRole(currentUser.getRole());

        return ResponseEntity.ok(userService.updateUser(currentUser.getId(), user));
    }

    // NEW: User can update their name only
    @PutMapping("/me/name")
    public ResponseEntity<User> updateCurrentUserName(@RequestBody Map<String, String> request) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        String newName = request.get("fullName");
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User updatedUser = userService.updateUserName(currentUser.getId(), newName.trim());
        return ResponseEntity.ok(updatedUser);
    }

    // NEW: User can update their password
    @PutMapping("/me/password")
    public ResponseEntity<Void> updateCurrentUserPassword(@RequestBody Map<String, String> request) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null ||
                currentPassword.isEmpty() || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().build();
        }

        try {
            userService.updateUserPassword(currentUser.getId(), currentPassword, newPassword);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // User can delete their own account
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser() {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        userService.deleteUser(currentUser.getId());
        return ResponseEntity.ok().build();
    }
}