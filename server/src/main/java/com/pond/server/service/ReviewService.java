package com.pond.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pond.server.dto.CreateReviewRequest;
import com.pond.server.dto.ReviewDTO;
import com.pond.server.dto.UpdateReviewRequest;
import com.pond.server.dto.UserRatingStatsDTO;
import com.pond.server.model.Listing;
import com.pond.server.model.Review;
import com.pond.server.model.User;
import com.pond.server.repository.ListingRepository;
import com.pond.server.repository.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ListingRepository listingRepository;

    public ReviewService(ReviewRepository reviewRepository, ListingRepository listingRepository){
        this.reviewRepository = reviewRepository;
        this.listingRepository = listingRepository;
    }

    @Transactional
    public ReviewDTO createReview(CreateReviewRequest request, UUID reviewerGu){
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5.");
        }

        if (request.getComment() == null || request.getComment().trim().isEmpty()) {
            throw new RuntimeException("Review comment cannot be empty!");
        }

        UUID revieweeGu = request.getRevieweeGU();

        if (reviewerGu.equals(revieweeGu)) {
            throw new RuntimeException("You cannot review yourself.");
        }

        // Dupe Check
        if (reviewRepository.existsByReviewerGuAndRevieweeGu(reviewerGu, revieweeGu)) {
            throw new RuntimeException("You have already reviewed this person.");
        }

        // Find a listing where these two users had a transaction
        // Either: reviewer is seller and reviewee is buyer, OR reviewer is buyer and reviewee is seller
        List<Listing> sellerToBuyerListings = listingRepository.findByUserGUAndSoldTo(reviewerGu, revieweeGu);
        List<Listing> buyerToSellerListings = listingRepository.findByUserGUAndSoldTo(revieweeGu, reviewerGu);

        Listing transactionListing = null;
        if (!sellerToBuyerListings.isEmpty()) {
            transactionListing = sellerToBuyerListings.get(0); // If theres multiple listings just grab one arbitrarily
        } else if (!buyerToSellerListings.isEmpty()) {
            transactionListing = buyerToSellerListings.get(0);
        }

        if (transactionListing == null) {
            throw new RuntimeException("No transaction found between you and this user.");
        }

        // TODO: Add the review type logic later.
//        // Determine review_type based on whether if listing is sold
//        ReviewType reviewType = (transactionListing.getSold() != null && transactionListing.getSold())
//                ? ReviewType.TRANSACTION
//                : ReviewType.CONVERSATION;

        // Create review
        Review review = new Review();
        review.setReviewerGu(reviewerGu);
        review.setRevieweeGu(revieweeGu);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setTimestamp(LocalDateTime.now());
        review = reviewRepository.save(review);
        
        // Fetch the review with user info in one query
        return reviewRepository.findReviewWithReviewerInfoById(review.getId())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve created review"));
    }

    @Transactional
    public ReviewDTO updateReview(UUID reviewId, UpdateReviewRequest request, UUID reviewerGu) {

        // Get the review
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found."));

        // Verify ownership
        if (!existingReview.getReviewerGu().equals(reviewerGu)){
            throw new RuntimeException("Not authorized to update this review.");
        }

        if (request.getRating() != null) {
            if (request.getRating() < 1 || request.getRating() > 5){
                throw new RuntimeException("Rating must be between 1 and 5");
            }
            existingReview.setRating(request.getRating());
        }

        if (request.getComment() != null && !request.getComment().trim().isEmpty()) {
            existingReview.setComment(request.getComment());
        } else {
            throw new RuntimeException("Review comment cannot be empty!");
        }

        existingReview.setUpdatedAt(LocalDateTime.now());
        Review updatedReview = reviewRepository.save(existingReview);
        
        // Fetch the review with user info in one query
        return reviewRepository.findReviewWithReviewerInfoById(updatedReview.getId())
                .orElseThrow(() -> new RuntimeException("Failed to retrieve updated review"));
    }

    @Transactional
    public String userDeleteReview(UUID reviewId, UUID reviewerGu){

        Review reviewToDelete = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found."));

        // Verify ownership
        if (!reviewToDelete.getReviewerGu().equals(reviewerGu)){
            throw new RuntimeException("Not authorized to delete a review you do not own.");
        }
        reviewRepository.deleteById(reviewId);

        return "Review deleted successfully";
    }

    @Transactional
    public String adminDeleteReview(UUID reviewId, User currentUser){
        Review reviewToDelete;

        if (currentUser.getAdmin()){
            reviewToDelete = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found."));
        } else {
            throw new RuntimeException("Not authorized to delete a review.");
        }

        reviewRepository.deleteById(reviewId);

        return "Review deleted successfully";
    }

    // For the front end to display review stats
    public List<ReviewDTO> getReviewsForUser(UUID userGu){
        // Use the optimized query that fetches reviews with reviewer info in a single query
        return reviewRepository.findReviewsWithReviewerInfoByRevieweeGu(userGu);
    }

    // Check if a user can review another user
    public boolean canUserReview(UUID reviewerGu, UUID revieweeGu){

        // Can't review yourself
        if (reviewerGu.equals(revieweeGu)){
            return false;
        }

        // Check if they already reviewed
        if (reviewRepository.existsByReviewerGuAndRevieweeGu(reviewerGu, revieweeGu)){
            return false;
        }


        boolean canReview = listingRepository.didTransactionOccur(reviewerGu, revieweeGu);

        return canReview;
    }

    // For the front end to display review stats
    public UserRatingStatsDTO getUserRatingStats(UUID userGu, UUID currentUserGu) {
        Long totalReviews = reviewRepository.countByRevieweeGu(userGu);
        Double averageRating = reviewRepository.findAverageRatingByRevieweeGu(userGu);

        if (averageRating == null) {
            averageRating = 0.0;
        }

        // Check if current user can review this user
        Boolean canReview = null;
        if (currentUserGu != null) {
            canReview = canUserReview(currentUserGu, userGu);
        }

        return new UserRatingStatsDTO(userGu, averageRating, totalReviews, canReview);
    }
}
