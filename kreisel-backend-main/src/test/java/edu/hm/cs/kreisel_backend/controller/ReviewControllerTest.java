package edu.hm.cs.kreisel_backend.controller;

import edu.hm.cs.kreisel_backend.model.Item;
import edu.hm.cs.kreisel_backend.model.Review;
import edu.hm.cs.kreisel_backend.model.User;
import edu.hm.cs.kreisel_backend.security.SecurityUtils;
import edu.hm.cs.kreisel_backend.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ReviewController reviewController;

    private User testUser;
    private Item testItem;
    private Review testReview;
    private List<Review> reviewList;
    private Long itemId;
    private Long rentalId;
    private Long reviewId;
    private LocalDateTime reviewTime;

    @BeforeEach
    void setUp() {
        itemId = 1L;
        rentalId = 2L;
        reviewId = 3L;
        reviewTime = LocalDateTime.now();

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");
        testUser.setEmail("user@hm.edu");
        testUser.setRole(User.Role.USER);

        // Setup test item
        testItem = new Item();
        testItem.setId(itemId);
        testItem.setName("Test Item");

        // Setup test review
        testReview = new Review();
        testReview.setId(reviewId);
        testReview.setItem(testItem);
        testReview.setUser(testUser);
        testReview.setRating(4);
        testReview.setComment("Great item!");
        testReview.setCreatedAt(reviewTime);

        // Setup review list
        reviewList = Collections.singletonList(testReview);
    }

    @Test
    void getItemReviews_ShouldReturnReviewsAndStats() {
        // Given
        when(reviewService.getReviewsByItemId(itemId)).thenReturn(reviewList);
        when(reviewService.getAverageRatingForItem(itemId)).thenReturn(4.0);

        // When
        ResponseEntity<Map<String, Object>> response = reviewController.getItemReviews(itemId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = response.getBody();
        assertEquals(reviewList, responseBody.get("reviews"));
        assertEquals(4.0, responseBody.get("averageRating"));
        assertEquals(1, responseBody.get("count"));

        verify(reviewService).getReviewsByItemId(itemId);
        verify(reviewService).getAverageRatingForItem(itemId);
    }

    @Test
    void getItemReviews_WithNoReviews_ShouldReturnEmptyListAndZeroAverage() {
        // Given
        when(reviewService.getReviewsByItemId(itemId)).thenReturn(Collections.emptyList());
        when(reviewService.getAverageRatingForItem(itemId)).thenReturn(null);

        // When
        ResponseEntity<Map<String, Object>> response = reviewController.getItemReviews(itemId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = response.getBody();
        assertEquals(Collections.emptyList(), responseBody.get("reviews"));
        assertEquals(0.0, responseBody.get("averageRating"));
        assertEquals(0, responseBody.get("count"));

        verify(reviewService).getReviewsByItemId(itemId);
        verify(reviewService).getAverageRatingForItem(itemId);
    }

    @Test
    void createReview_WithValidData_ShouldCreateReview() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(reviewService.createReview(rentalId, testUser.getId(), 4, "Great item!")).thenReturn(testReview);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("rentalId", rentalId);
        requestBody.put("rating", 4);
        requestBody.put("comment", "Great item!");

        // When
        ResponseEntity<Map<String, Object>> response = reviewController.createReview(requestBody);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = response.getBody();
        assertEquals(reviewId, responseBody.get("id"));
        assertEquals(4, responseBody.get("rating"));
        assertEquals("Great item!", responseBody.get("comment"));
        assertEquals(reviewTime.toString(), responseBody.get("createdAt"));
        assertEquals(true, responseBody.get("success"));

        verify(securityUtils).getCurrentUser();
        verify(reviewService).createReview(rentalId, testUser.getId(), 4, "Great item!");
    }

    @Test
    void createReview_WithInvalidRating_ShouldReturnBadRequest() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("rentalId", rentalId);
        requestBody.put("rating", 6); // Invalid rating (> 5)
        requestBody.put("comment", "Great item!");

        // When
        ResponseEntity<Map<String, Object>> response = reviewController.createReview(requestBody);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = response.getBody();
        assertEquals("Rating must be between 1 and 5", responseBody.get("error"));

        verify(securityUtils).getCurrentUser();
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void createReview_WithTooLowRating_ShouldReturnBadRequest() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("rentalId", rentalId);
        requestBody.put("rating", 0); // Invalid rating (< 1)
        requestBody.put("comment", "Great item!");

        // When
        ResponseEntity<Map<String, Object>> response = reviewController.createReview(requestBody);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = response.getBody();
        assertEquals("Rating must be between 1 and 5", responseBody.get("error"));

        verify(securityUtils).getCurrentUser();
        verifyNoMoreInteractions(reviewService);
    }

    @Test
    void createReview_WhenServiceThrowsException_ShouldReturnBadRequest() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);
        when(reviewService.createReview(rentalId, testUser.getId(), 4, "Great item!"))
                .thenThrow(new IllegalArgumentException("Already reviewed"));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("rentalId", rentalId);
        requestBody.put("rating", 4);
        requestBody.put("comment", "Great item!");

        // When
        ResponseEntity<Map<String, Object>> response = reviewController.createReview(requestBody);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> responseBody = response.getBody();
        assertEquals("Already reviewed", responseBody.get("error"));

        verify(securityUtils).getCurrentUser();
        verify(reviewService).createReview(rentalId, testUser.getId(), 4, "Great item!");
    }

    @Test
    void createReview_WhenUnauthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("rentalId", rentalId);
        requestBody.put("rating", 4);
        requestBody.put("comment", "Great item!");

        // When
        ResponseEntity<Map<String, Object>> response = reviewController.createReview(requestBody);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(reviewService);
    }

    @Test
    void canReviewRental_WhenEligible_ShouldReturnEligibilityInfo() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);

        Map<String, Object> eligibilityInfo = new HashMap<>();
        eligibilityInfo.put("canReview", true);
        eligibilityInfo.put("reason", null);

        when(reviewService.checkReviewEligibility(rentalId, testUser.getId())).thenReturn(eligibilityInfo);

        // When
        ResponseEntity<Map<String, Object>> response = reviewController.canReviewRental(rentalId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(eligibilityInfo, response.getBody());

        verify(securityUtils).getCurrentUser();
        verify(reviewService).checkReviewEligibility(rentalId, testUser.getId());
    }

    @Test
    void canReviewRental_WhenNotEligible_ShouldReturnEligibilityInfo() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(testUser);

        Map<String, Object> eligibilityInfo = new HashMap<>();
        eligibilityInfo.put("canReview", false);
        eligibilityInfo.put("reason", "Rental not completed");

        when(reviewService.checkReviewEligibility(rentalId, testUser.getId())).thenReturn(eligibilityInfo);

        // When
        ResponseEntity<Map<String, Object>> response = reviewController.canReviewRental(rentalId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(eligibilityInfo, response.getBody());

        verify(securityUtils).getCurrentUser();
        verify(reviewService).checkReviewEligibility(rentalId, testUser.getId());
    }

    @Test
    void canReviewRental_WhenUnauthenticated_ShouldReturnUnauthorized() {
        // Given
        when(securityUtils.getCurrentUser()).thenReturn(null);

        // When
        ResponseEntity<Map<String, Object>> response = reviewController.canReviewRental(rentalId);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        verify(securityUtils).getCurrentUser();
        verifyNoInteractions(reviewService);
    }
}