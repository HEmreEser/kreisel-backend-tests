package edu.hm.cs.kreisel_backend.service;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.model.Rental;
import edu.hm.cs.kreisel_backend.model.Review;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.repository.ItemRepository;
import edu.hm.cs.kreisel_backend.repository.RentalRepository;
import edu.hm.cs.kreisel_backend.repository.ReviewRepository;
import edu.hm.cs.kreisel_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Item testItem;
    private Rental completedRental;
    private Rental activeRental;
    private Rental otherUserRental;
    private Review existingReview;
    private List<Review> itemReviews;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("test@hm.edu");
        testUser.setRole(User.Role.USER);

        // Setup another user
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setFullName("Other User");
        otherUser.setEmail("other@hm.edu");
        otherUser.setRole(User.Role.USER);

        // Setup test item
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setAverageRating(4.0);
        testItem.setReviewCount(2);

        // Setup completed rental (eligible for review)
        completedRental = new Rental();
        completedRental.setId(1L);
        completedRental.setUser(testUser);
        completedRental.setItem(testItem);
        completedRental.setRentalDate(LocalDate.now().minusDays(14));
        completedRental.setEndDate(LocalDate.now().minusDays(7));
        completedRental.setReturnDate(LocalDate.now().minusDays(7));

        // Setup active rental (not eligible for review - not yet returned)
        activeRental = new Rental();
        activeRental.setId(2L);
        activeRental.setUser(testUser);
        activeRental.setItem(testItem);
        activeRental.setRentalDate(LocalDate.now().minusDays(7));
        activeRental.setEndDate(LocalDate.now().plusDays(7));
        activeRental.setReturnDate(null);

        // Setup other user's rental
        otherUserRental = new Rental();
        otherUserRental.setId(3L);
        otherUserRental.setUser(otherUser);
        otherUserRental.setItem(testItem);
        otherUserRental.setRentalDate(LocalDate.now().minusDays(10));
        otherUserRental.setEndDate(LocalDate.now().minusDays(5));
        otherUserRental.setReturnDate(LocalDate.now().minusDays(5));

        // Setup existing review
        existingReview = new Review();
        existingReview.setId(1L);
        existingReview.setUser(testUser);
        existingReview.setItem(testItem);
        existingReview.setRental(completedRental);
        existingReview.setRating(4);
        existingReview.setComment("Great item!");
        existingReview.setCreatedAt(LocalDateTime.now().minusDays(5));

        // Setup item reviews
        itemReviews = Arrays.asList(
                existingReview,
                new Review() {{
                    setId(2L);
                    setUser(otherUser);
                    setItem(testItem);
                    setRental(otherUserRental);
                    setRating(5);
                    setComment("Excellent!");
                    setCreatedAt(LocalDateTime.now().minusDays(3));
                }}
        );
    }

    @Test
    void getReviewsByItemId_ShouldReturnReviewsForItem() {
        // Given
        when(reviewRepository.findByItemId(testItem.getId())).thenReturn(itemReviews);

        // When
        List<Review> result = reviewService.getReviewsByItemId(testItem.getId());

        // Then
        assertEquals(2, result.size());
        assertEquals(itemReviews, result);
        verify(reviewRepository).findByItemId(testItem.getId());
    }

    @Test
    void getAverageRatingForItem_WhenReviewsExist_ShouldReturnAverage() {
        // Given
        Double expectedAverage = 4.5;
        when(reviewRepository.getAverageRatingForItem(testItem.getId())).thenReturn(expectedAverage);

        // When
        Double result = reviewService.getAverageRatingForItem(testItem.getId());

        // Then
        assertEquals(expectedAverage, result);
        verify(reviewRepository).getAverageRatingForItem(testItem.getId());
    }

    @Test
    void getAverageRatingForItem_WhenNoReviews_ShouldReturnNull() {
        // Given
        when(reviewRepository.getAverageRatingForItem(testItem.getId())).thenReturn(null);

        // When
        Double result = reviewService.getAverageRatingForItem(testItem.getId());

        // Then
        assertNull(result);
        verify(reviewRepository).getAverageRatingForItem(testItem.getId());
    }

    @Test
    void checkReviewEligibility_WhenEligible_ShouldReturnTrue() {
        // Given
        long rentalId = 4L;
        Rental eligibleRental = new Rental();
        eligibleRental.setId(rentalId);
        eligibleRental.setUser(testUser);
        eligibleRental.setReturnDate(LocalDate.now().minusDays(1));

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(eligibleRental));
        when(reviewRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = reviewService.checkReviewEligibility(rentalId, testUser.getId());

        // Then
        assertTrue((Boolean) result.get("canReview"));
        assertEquals("You can review this rental", result.get("message"));
        verify(rentalRepository).findById(rentalId);
        verify(reviewRepository).findByRentalId(rentalId);
    }

    @Test
    void checkReviewEligibility_WhenRentalNotFound_ShouldReturnFalse() {
        // Given
        long nonExistentRentalId = 99L;
        when(rentalRepository.findById(nonExistentRentalId)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = reviewService.checkReviewEligibility(nonExistentRentalId, testUser.getId());

        // Then
        assertFalse((Boolean) result.get("canReview"));
        assertEquals("Rental not found", result.get("message"));
        verify(rentalRepository).findById(nonExistentRentalId);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void checkReviewEligibility_WhenNotUserRental_ShouldReturnFalse() {
        // Given
        when(rentalRepository.findById(otherUserRental.getId())).thenReturn(Optional.of(otherUserRental));

        // When
        Map<String, Object> result = reviewService.checkReviewEligibility(otherUserRental.getId(), testUser.getId());

        // Then
        assertFalse((Boolean) result.get("canReview"));
        assertEquals("This rental doesn't belong to you", result.get("message"));
        verify(rentalRepository).findById(otherUserRental.getId());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void checkReviewEligibility_WhenItemNotReturned_ShouldReturnFalse() {
        // Given
        when(rentalRepository.findById(activeRental.getId())).thenReturn(Optional.of(activeRental));

        // When
        Map<String, Object> result = reviewService.checkReviewEligibility(activeRental.getId(), testUser.getId());

        // Then
        assertFalse((Boolean) result.get("canReview"));
        assertEquals("You need to return the item before reviewing", result.get("message"));
        verify(rentalRepository).findById(activeRental.getId());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void checkReviewEligibility_WhenAlreadyReviewed_ShouldReturnFalse() {
        // Given
        Long rentalWithReviewId = completedRental.getId();
        when(rentalRepository.findById(rentalWithReviewId)).thenReturn(Optional.of(completedRental));
        when(reviewRepository.findByRentalId(rentalWithReviewId)).thenReturn(Optional.of(existingReview));

        // When
        Map<String, Object> result = reviewService.checkReviewEligibility(rentalWithReviewId, testUser.getId());

        // Then
        assertFalse((Boolean) result.get("canReview"));
        assertEquals("You have already reviewed this rental", result.get("message"));
        assertEquals(existingReview.getId(), result.get("existingReviewId"));
        verify(rentalRepository).findById(rentalWithReviewId);
        verify(reviewRepository).findByRentalId(rentalWithReviewId);
    }

    @Test
    void createReview_WhenEligible_ShouldCreateAndUpdateItemStats() {
        // Given
        Long rentalId = 4L;
        Long userId = testUser.getId();
        int rating = 5;
        String comment = "Excellent item!";

        // Create a new eligible rental that doesn't have a review yet
        Rental eligibleRental = new Rental();
        eligibleRental.setId(rentalId);
        eligibleRental.setUser(testUser);
        eligibleRental.setItem(testItem);
        eligibleRental.setReturnDate(LocalDate.now().minusDays(1));

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(eligibleRental));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(reviewRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            savedReview.setId(3L);  // Assign an ID to simulate saving
            return savedReview;
        });

        Double newAvgRating = 4.3;
        List<Review> updatedReviews = new ArrayList<>(itemReviews);
        updatedReviews.add(new Review()); // Add one more review

        when(reviewRepository.getAverageRatingForItem(testItem.getId())).thenReturn(newAvgRating);
        when(reviewRepository.findByItemId(testItem.getId())).thenReturn(updatedReviews);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Review result = reviewService.createReview(rentalId, userId, rating, comment);

        // Then
        // Verify review was created correctly
        assertEquals(3L, result.getId());
        assertEquals(testUser, result.getUser());
        assertEquals(testItem, result.getItem());
        assertEquals(eligibleRental, result.getRental());
        assertEquals(rating, result.getRating());
        assertEquals(comment, result.getComment());
        assertNotNull(result.getCreatedAt());

        // Verify item stats were updated
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        Item updatedItem = itemCaptor.getValue();
        assertEquals(newAvgRating, updatedItem.getAverageRating());
        assertEquals(3, updatedItem.getReviewCount()); // 2 original + 1 new

        // Verify repository calls
        verify(rentalRepository).findById(rentalId);
        verify(reviewRepository).findByRentalId(rentalId);
        verify(userRepository).findById(userId);
        verify(reviewRepository).save(any(Review.class));
        verify(reviewRepository).getAverageRatingForItem(testItem.getId());
        verify(reviewRepository).findByItemId(testItem.getId());
    }

    @Test
    void createReview_WhenRentalNotFound_ShouldThrowException() {
        // Given
        Long nonExistentRentalId = 99L;
        when(rentalRepository.findById(nonExistentRentalId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(nonExistentRentalId, testUser.getId(), 5, "Great!");
        });

        assertEquals("Rental not found", exception.getMessage());
        verify(rentalRepository).findById(nonExistentRentalId);
        verifyNoInteractions(userRepository, reviewRepository, itemRepository);
    }

    @Test
    void createReview_WhenNotUserRental_ShouldThrowException() {
        // Given
        when(rentalRepository.findById(otherUserRental.getId())).thenReturn(Optional.of(otherUserRental));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(otherUserRental.getId(), testUser.getId(), 5, "Great!");
        });

        assertEquals("This rental doesn't belong to you", exception.getMessage());
        verify(rentalRepository).findById(otherUserRental.getId());
        verifyNoInteractions(userRepository, reviewRepository, itemRepository);
    }

    @Test
    void createReview_WhenItemNotReturned_ShouldThrowException() {
        // Given
        when(rentalRepository.findById(activeRental.getId())).thenReturn(Optional.of(activeRental));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(activeRental.getId(), testUser.getId(), 5, "Great!");
        });

        assertEquals("Item must be returned before reviewing", exception.getMessage());
        verify(rentalRepository).findById(activeRental.getId());
        verifyNoInteractions(userRepository, reviewRepository, itemRepository);
    }

    @Test
    void createReview_WhenAlreadyReviewed_ShouldThrowException() {
        // Given
        Long rentalWithReviewId = completedRental.getId();
        when(rentalRepository.findById(rentalWithReviewId)).thenReturn(Optional.of(completedRental));
        when(reviewRepository.findByRentalId(rentalWithReviewId)).thenReturn(Optional.of(existingReview));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(rentalWithReviewId, testUser.getId(), 5, "Great!");
        });

        assertEquals("You have already reviewed this rental", exception.getMessage());
        verify(rentalRepository).findById(rentalWithReviewId);
        verify(reviewRepository).findByRentalId(rentalWithReviewId);
        verifyNoInteractions(userRepository, itemRepository);
    }

    @Test
    void createReview_WhenUserNotFound_ShouldThrowException() {
        // Given
        Long rentalId = 4L;
        Long nonExistentUserId = 99L;

        // Create a new eligible rental
        Rental eligibleRental = new Rental();
        eligibleRental.setId(rentalId);
        eligibleRental.setUser(testUser);
        eligibleRental.setReturnDate(LocalDate.now().minusDays(1));

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(eligibleRental));
        when(reviewRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(rentalId, nonExistentUserId, 5, "Great!");
        });

        assertEquals("User not found", exception.getMessage());
        verify(rentalRepository).findById(rentalId);
        verify(reviewRepository).findByRentalId(rentalId);
        verify(userRepository).findById(nonExistentUserId);
        verifyNoInteractions(itemRepository);
    }

    @Test
    void createReview_WhenNullAvgRating_ShouldUseZero() {
        // Given
        Long rentalId = 4L;

        // Create a new eligible rental
        Rental eligibleRental = new Rental();
        eligibleRental.setId(rentalId);
        eligibleRental.setUser(testUser);
        eligibleRental.setItem(testItem);
        eligibleRental.setReturnDate(LocalDate.now().minusDays(1));

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(eligibleRental));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(reviewRepository.findByRentalId(rentalId)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Return null for average rating
        when(reviewRepository.getAverageRatingForItem(testItem.getId())).thenReturn(null);

        List<Review> updatedReviews = new ArrayList<>(itemReviews);
        when(reviewRepository.findByItemId(testItem.getId())).thenReturn(updatedReviews);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        reviewService.createReview(rentalId, testUser.getId(), 5, "Great!");

        // Then
        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        Item updatedItem = itemCaptor.getValue();
        assertEquals(0.0, updatedItem.getAverageRating()); // Should use 0.0 when null
    }
}