package com.pond.server.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pond.server.dto.CreateReviewRequest;
import com.pond.server.dto.ReviewDTO;
import com.pond.server.dto.UpdateReviewRequest;
import com.pond.server.dto.UserRatingStatsDTO;
import com.pond.server.model.User;
import com.pond.server.service.ReviewService;

/**
 * REST controller for review management operations.
 * Handles creation, updating, deletion, and retrieval of user reviews.
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Constructs a new ReviewController with required dependencies.
     *
     * @param reviewService the service for review operations
     */
    public ReviewController(ReviewService reviewService){
        this.reviewService = reviewService;
    }

    /**
     * Creates a new review for another user.
     * Requires a transaction between the users.
     *
     * @param user the authenticated user creating the review
     * @param request the create review request with rating and comment
     * @return ResponseEntity with the created review
     */
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            @AuthenticationPrincipal User user,
            @RequestBody CreateReviewRequest request){
        return ResponseEntity.ok(reviewService.createReview(request, user.getUserGU()));
    }

    /**
     * Updates an existing review owned by the authenticated user.
     *
     * @param user the authenticated user
     * @param reviewId the UUID of the review to update
     * @param request the update request with new rating and/or comment
     * @return ResponseEntity with the updated review
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> updateReview(
            @AuthenticationPrincipal User user,
            @PathVariable UUID reviewId,
            @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request, user.getUserGU()));
    }

    /**
     * Deletes a review owned by the authenticated user.
     *
     * @param user the authenticated user
     * @param reviewId the UUID of the review to delete
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable UUID reviewId) {
        reviewService.userDeleteReview(reviewId, user.getUserGU());
        return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
    }

    /**
     * Deletes any review (admin only).
     *
     * @param user the authenticated admin user
     * @param reviewId the UUID of the review to delete
     * @return ResponseEntity with success message or 403 if not admin
     */
    @DeleteMapping("/admin/{reviewId}")
    public ResponseEntity<Map<String, String>> adminDeleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable UUID reviewId) {
        // Explicit admin check
        if (!Boolean.TRUE.equals(user.getAdmin())) {
            return ResponseEntity.status(403).build();
        }
        reviewService.adminDeleteReview(reviewId, user);
        return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
    }

    /**
     * Retrieves all reviews for a specific user.
     *
     * @param userGu the UUID of the user (reviewee)
     * @return ResponseEntity with list of reviews
     */
    @GetMapping("/user/{userGu}")
    public ResponseEntity<List<ReviewDTO>> getReviewsForUser(@PathVariable UUID userGu) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(userGu));
    }

    /**
     * Retrieves rating statistics for a user.
     * Includes average rating, total reviews, and whether current user can review them.
     *
     * @param user the authenticated user (optional)
     * @param userGu the UUID of the user whose stats to retrieve
     * @return ResponseEntity with rating statistics
     */
    @GetMapping("/stats/{userGu}")
    public ResponseEntity<UserRatingStatsDTO> getUserRatingStats(
            @AuthenticationPrincipal User user,
            @PathVariable UUID userGu) {
        UUID currentUserGu = user != null ? user.getUserGU() : null;
        return ResponseEntity.ok(reviewService.getUserRatingStats(userGu, currentUserGu));
    }

    /**
     * Checks if the authenticated user can review another user.
     *
     * @param user the authenticated user
     * @param userGu the UUID of the potential reviewee
     * @return ResponseEntity with boolean indicating if review is allowed
     */
    @GetMapping("/can-review/{userGu}")
    public ResponseEntity<Boolean> canReviewUser(
            @AuthenticationPrincipal User user,
            @PathVariable UUID userGu) {
        boolean canReview = reviewService.canUserReview(user.getUserGU(), userGu);
        return ResponseEntity.ok(canReview);
    }
}
