package edu.hm.cs.kreisel_backend.controller;

import edu.hm.cs.kreisel_backend.model.Review;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.security.SecurityUtils;
import edu.hm.cs.kreisel_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final SecurityUtils securityUtils;

    // Get reviews for an item
    @GetMapping("/item/{itemId}")
    public ResponseEntity<Map<String, Object>> getItemReviews(@PathVariable Long itemId) {
        List<Review> reviews = reviewService.getReviewsByItemId(itemId);
        Double average = reviewService.getAverageRatingForItem(itemId);

        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviews);
        response.put("averageRating", average != null ? average : 0.0);
        response.put("count", reviews.size());

        return ResponseEntity.ok(response);
    }

    // Create a review
    @PostMapping
    public ResponseEntity<Map<String, Object>> createReview(@RequestBody Map<String, Object> request) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            // Extract request data
            Long rentalId = ((Number) request.get("rentalId")).longValue();
            Integer rating = ((Number) request.get("rating")).intValue();
            String comment = (String) request.get("comment");

            // Validate rating
            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Rating must be between 1 and 5"
                ));
            }

            // Create review
            Review review = reviewService.createReview(rentalId, currentUser.getId(), rating, comment);

            // Return JSON response
            Map<String, Object> response = new HashMap<>();
            response.put("id", review.getId());
            response.put("rating", review.getRating());
            response.put("comment", review.getComment());
            response.put("createdAt", review.getCreatedAt().toString());
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // Check if user can review a rental
    @GetMapping("/can-review/{rentalId}")
    public ResponseEntity<Map<String, Object>> canReviewRental(@PathVariable Long rentalId) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> result = reviewService.checkReviewEligibility(rentalId, currentUser.getId());
        return ResponseEntity.ok(result);
    }
}