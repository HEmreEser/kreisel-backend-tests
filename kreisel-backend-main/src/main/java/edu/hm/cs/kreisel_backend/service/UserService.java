package edu.hm.cs.kreisel_backend.service;

import edu.hm.cs.kreisel_backend.model.Rental;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Rental> getRentalsByUserId(Long userId) {
        User user = getUserById(userId);
        return user.getRentals();
    }

    public User createUser(User user) {
        // Passwort hashen falls es noch nicht gehashed ist
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Role assignment is now handled in AuthService
        if (user.getRole() == null) {
            user.setRole(User.Role.USER); // Default role
        }

        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);
        existing.setFullName(updatedUser.getFullName());
        existing.setEmail(updatedUser.getEmail());

        // Passwort nur aktualisieren wenn neues angegeben wurde
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        existing.setRole(updatedUser.getRole());
        return userRepository.save(existing);
    }

    // NEW: Update only user name
    public User updateUserName(Long id, String newName) {
        User existing = getUserById(id);
        existing.setFullName(newName);
        return userRepository.save(existing);
    }

    // NEW: Update user password with current password verification
    public void updateUserPassword(Long id, String currentPassword, String newPassword) {
        User existing = getUserById(id);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, existing.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Set new password
        existing.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}