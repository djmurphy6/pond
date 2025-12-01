package com.pond.server.controller;

import com.pond.server.dto.CreateReviewRequest;
import com.pond.server.dto.ReviewDTO;
import com.pond.server.dto.UpdateReviewRequest;
import com.pond.server.dto.UserRatingStatsDTO;
import com.pond.server.model.User;
import com.pond.server.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService){
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            @AuthenticationPrincipal User user,
            @RequestBody CreateReviewRequest request){
        return ResponseEntity.ok(reviewService.createReview(request, user.getUserGU()));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> updateReview(
            @AuthenticationPrincipal User user,
            @PathVariable UUID reviewId,
            @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request, user.getUserGU()));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable UUID reviewId) {
        reviewService.userDeleteReview(reviewId, user.getUserGU());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/{reviewId}")
    public ResponseEntity<Void> adminDeleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable UUID reviewId) {
        // Explicit admin check
        if (!Boolean.TRUE.equals(user.getAdmin())) {
            return ResponseEntity.status(403).build();
        }
        reviewService.adminDeleteReview(reviewId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userGu}")
    public ResponseEntity<List<ReviewDTO>> getReviewsForUser(@PathVariable UUID userGu) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(userGu));
    }

    @GetMapping("/stats/{userGu}")
    public ResponseEntity<UserRatingStatsDTO> getUserRatingStats(
            @AuthenticationPrincipal User user,
            @PathVariable UUID userGu) {
        UUID currentUserGu = user != null ? user.getUserGU() : null;
        return ResponseEntity.ok(reviewService.getUserRatingStats(userGu, currentUserGu));
    }

    // Dedicated call for just if a user can review
    @GetMapping("/can-review/{userGu}")
    public ResponseEntity<Boolean> canReviewUser(
            @AuthenticationPrincipal User user,
            @PathVariable UUID userGu) {
        boolean canReview = reviewService.canUserReview(user.getUserGU(), userGu);
        return ResponseEntity.ok(canReview);
    }
}
