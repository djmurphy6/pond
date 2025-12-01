package com.pond.server.service;

import com.pond.server.dto.CreateReviewRequest;
import com.pond.server.dto.ReviewDTO;
import com.pond.server.dto.UpdateReviewRequest;
import com.pond.server.dto.UserRatingStatsDTO;
import com.pond.server.enums.ReviewType;
import com.pond.server.model.Listing;
import com.pond.server.model.Review;
import com.pond.server.model.User;
import com.pond.server.repository.ListingRepository;
import com.pond.server.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5){
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        // Validation: Comment cannot be empty on create
        if (request.getComment() == null || request.getComment().trim().isEmpty()) {
            throw new RuntimeException("Review comment cannot be empty!");
        }

        // Get the listing
        Listing listing = listingRepository.findById(request.getListingGU())
                .orElseThrow(() -> new RuntimeException("Listing not found!"));

        // TODO: Need to add this methods for it to work
        boolean isReviewerSeller = listing.getUserGU().equals(reviewerGu);
        boolean isReviewerBuyer = listing.getSoldTo() != null && listing.getSoldTo().equals(reviewerGu);

        if (!isReviewerSeller && !isReviewerBuyer) {
            throw new RuntimeException("You are not authorized to review this transaction");
        }

        UUID revieweeGu;
        if(isReviewerSeller){
            // Seller reviewing the buyer
            revieweeGu = listing.getSoldTo();
            if(revieweeGu == null){
                throw new RuntimeException("Cannot review: No buyer for this listing.");
            }
        } else {
            // Buyer reviewing the seller
            revieweeGu = listing.getUserGU();
        }


        if (reviewerGu.equals(revieweeGu)){
            throw new RuntimeException("You cannot review yourself");
        }

        // Check for dupes
        if (reviewRepository.existsByReviewerGuAndRevieweeGuAndListingGU(reviewerGu, revieweeGu, request.getListingGU())) {
            throw new RuntimeException("You have already reviewed this person for this listing");
        }

        ReviewType reviewType;
        if (listing.getSold() != null && listing.getSold()){
            // This is a review for a transaction
            reviewType = ReviewType.TRANSACTION;
        } else {
            //TODO: Check for 5+ messages in the chatroom
            reviewType = ReviewType.CONVERSATION;
        }

        // Create and populate Review entity
        Review review = new Review();
        review.setReviewerGu(reviewerGu);
        review.setRevieweeGu(revieweeGu);
        review.setListingGU(request.getListingGU());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setReviewType(reviewType);
        review.setTimestamp(LocalDateTime.now());

        // Save to DB
        review = reviewRepository.save(review);

        // Convert to DTO and return
        return toDto(review);

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

        if (request.getComment() == null){
            throw new RuntimeException("Review comment cannot be empty!"); // In the DTO length is limited to 500 chars
        }


        // Now we just need to update that mf
        existingReview.setComment(request.getComment());
        existingReview.setUpdatedAt(LocalDateTime.now());
        Review updatedReview = reviewRepository.save(existingReview); // save it
        return toDto(updatedReview);
    }

    @Transactional
    public void userDeleteReview(UUID reviewId, UUID reviewerGu){

        Review reviewToDelete = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found."));

        // Verify ownership
        if (!reviewToDelete.getReviewerGu().equals(reviewerGu)){
            throw new RuntimeException("Not authorized to delete a review you do not own.");
        }
        reviewRepository.deleteById(reviewId);
    }

    @Transactional
    public void adminDeleteReview(UUID reviewId, User currentUser){
        Review reviewToDelete;

        if (currentUser.getAdmin()){
            reviewToDelete = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new RuntimeException("Review not found."));
        } else {
            throw new RuntimeException("Not authorized to delete a review.");
        }

        reviewRepository.deleteById(reviewId);
    }

    // For the front end to display review stats
    public List<ReviewDTO> getReviewsForUser(UUID userGu){
        List<Review> reviews = reviewRepository.findReviewByRevieweeGu(userGu);
        return reviews.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // For the front end to display review stats
    public UserRatingStatsDTO getUserRatingStats(UUID userGu){
        Long totalReviews = reviewRepository.countByRevieweeGu(userGu);
        Double averageRating = reviewRepository.findAverageRatingByRevieweeGu(userGu);

        // Case for when we got no reviews
        if (averageRating == null){
            averageRating = 0.0;
        }
        return new UserRatingStatsDTO(userGu, averageRating, totalReviews);
    }

    private ReviewDTO toDto(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getReviewerGu(),
                review.getRevieweeGu(),
                review.getListingGU(),
                review.getRating(),
                review.getComment(),
                review.getReviewType(),
                review.getTimestamp(),
                review.getUpdatedAt()
        );
    }


}
