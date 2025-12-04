package com.pond.server.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pond.server.dto.ReviewDTO;
import com.pond.server.model.Review;

/**
 * Repository interface for {@link Review} entity database operations.
 * 
 * <p>Provides methods for managing user reviews including creation, retrieval,
 * and aggregation functions like calculating average ratings. Uses DTO projections
 * with JOIN queries to efficiently fetch reviewer information alongside reviews.</p>
 * 
 * @author Pond Team
 * @see Review
 * @see com.pond.server.dto.ReviewDTO
 */
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    
    /**
     * Finds all reviews written by a specific user.
     * 
     * @param reviewerGu UUID of the reviewer
     * @return list of reviews written by the user
     */
    List<Review> findReviewByReviewerGu(UUID reviewerGu);
    
    /**
     * Finds all reviews received by a specific user.
     * 
     * @param revieweeGu UUID of the user being reviewed
     * @return list of reviews for the user
     */
    List<Review> findReviewByRevieweeGu(UUID revieweeGu);
    
    /**
     * Finds all reviews created at a specific timestamp.
     * 
     * @param timestamp the timestamp to search for
     * @return list of reviews created at that time
     */
    List<Review> findReviewsByTimestamp(LocalDateTime timestamp);

    /**
     * Finds a review between two specific users.
     * Used to check if a review already exists.
     * 
     * @param reviewerGu UUID of the reviewer
     * @param revieweeGu UUID of the reviewee
     * @return an Optional containing the review if it exists
     */
    Optional<Review> findByReviewerGuAndRevieweeGu(UUID reviewerGu, UUID revieweeGu);

    /**
     * Checks if a review exists between two users.
     * Used to prevent duplicate reviews.
     * 
     * @param reviewerGu UUID of the reviewer
     * @param revieweeGu UUID of the reviewee
     * @return true if a review exists, false otherwise
     */
    boolean existsByReviewerGuAndRevieweeGu(UUID reviewerGu, UUID revieweeGu);

    /**
     * Calculates the average rating for a user.
     * Used to display overall seller rating.
     * 
     * @param revieweeGu UUID of the user being reviewed
     * @return average rating as a Double, null if no reviews exist
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeGu = :revieweeGu")
    Double findAverageRatingByRevieweeGu(@Param("revieweeGu") UUID revieweeGu);

    /**
     * Counts the total number of reviews received by a user.
     * 
     * @param revieweeGu UUID of the user being reviewed
     * @return count of reviews
     */
    Long countByRevieweeGu(UUID revieweeGu);

    /**
     * Fetches reviews with reviewer information using DTO projection.
     * OPTIMIZED: Uses JOIN to fetch reviewer details in a single query.
     * 
     * @param revieweeGu UUID of the user whose reviews to fetch
     * @return list of ReviewDTOs with reviewer information
     */
    @Query("SELECT new com.pond.server.dto.ReviewDTO(r.id, r.reviewerGu, r.revieweeGu, r.rating, r.comment, r.timestamp, r.updatedAt, u.username, u.avatar_url) " +
           "FROM Review r JOIN User u ON r.reviewerGu = u.id " +
           "WHERE r.revieweeGu = :revieweeGu")
    List<ReviewDTO> findReviewsWithReviewerInfoByRevieweeGu(@Param("revieweeGu") UUID revieweeGu);

    /**
     * Fetches a single review with reviewer information using DTO projection.
     * OPTIMIZED: Uses JOIN to fetch reviewer details in a single query.
     * 
     * @param reviewId UUID of the review
     * @return an Optional containing the ReviewDTO if found
     */
    @Query("SELECT new com.pond.server.dto.ReviewDTO(r.id, r.reviewerGu, r.revieweeGu, r.rating, r.comment, r.timestamp, r.updatedAt, u.username, u.avatar_url) " +
           "FROM Review r JOIN User u ON r.reviewerGu = u.id " +
           "WHERE r.id = :reviewId")
    Optional<ReviewDTO> findReviewWithReviewerInfoById(@Param("reviewId") UUID reviewId);
}
