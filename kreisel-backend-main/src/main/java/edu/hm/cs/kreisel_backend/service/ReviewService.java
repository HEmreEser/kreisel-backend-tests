package edu.hm.cs.kreisel_backend.service;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.model.Rental;
import edu.hm.cs.kreisel_backend.model.Review;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.repository.ItemRepository;
import edu.hm.cs.kreisel_backend.repository.RentalRepository;
import edu.hm.cs.kreisel_backend.repository.ReviewRepository;
import edu.hm.cs.kreisel_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final RentalRepository rentalRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    // Get all reviews for an item
    public List<Review> getReviewsByItemId(Long itemId) {
        return reviewRepository.findByItemId(itemId);
    }

    // Get average rating for an item
    public Double getAverageRatingForItem(Long itemId) {
        return reviewRepository.getAverageRatingForItem(itemId);
    }

    // Check if user can review a rental
    public Map<String, Object> checkReviewEligibility(Long rentalId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        // Find rental
        Optional<Rental> rentalOpt = rentalRepository.findById(rentalId);
        if (rentalOpt.isEmpty()) {
            result.put("canReview", false);
            result.put("message", "Rental not found");
            return result;
        }

        Rental rental = rentalOpt.get();

        // Check if rental belongs to user
        if (!rental.getUser().getId().equals(userId)) {
            result.put("canReview", false);
            result.put("message", "This rental doesn't belong to you");
            return result;
        }

        // Check if item has been returned
        if (rental.getReturnDate() == null) {
            result.put("canReview", false);
            result.put("message", "You need to return the item before reviewing");
            return result;
        }

        // Check if review already exists
        Optional<Review> existingReview = reviewRepository.findByRentalId(rentalId);
        if (existingReview.isPresent()) {
            result.put("canReview", false);
            result.put("message", "You have already reviewed this rental");
            result.put("existingReviewId", existingReview.get().getId());
            return result;
        }

        // User can review
        result.put("canReview", true);
        result.put("message", "You can review this rental");
        return result;
    }

    // Create a review
    @Transactional
    public Review createReview(Long rentalId, Long userId, int rating, String comment) {
        // Find rental
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        // Verify rental belongs to user
        if (!rental.getUser().getId().equals(userId)) {
            throw new RuntimeException("This rental doesn't belong to you");
        }

        // Check if rental is returned
        if (rental.getReturnDate() == null) {
            throw new RuntimeException("Item must be returned before reviewing");
        }

        // Check if review already exists
        if (reviewRepository.findByRentalId(rentalId).isPresent()) {
            throw new RuntimeException("You have already reviewed this rental");
        }

        // Create review
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Item item = rental.getItem();

        Review review = new Review();
        review.setItem(item);
        review.setUser(user);
        review.setRental(rental);
        review.setRating(rating);
        review.setComment(comment);

        // Save review
        Review savedReview = reviewRepository.save(review);

        // Update item rating statistics
        Double avgRating = reviewRepository.getAverageRatingForItem(item.getId());
        List<Review> itemReviews = reviewRepository.findByItemId(item.getId());

        item.setAverageRating(avgRating != null ? avgRating : 0.0);
        item.setReviewCount(itemReviews.size());
        itemRepository.save(item);

        return savedReview;
    }
}