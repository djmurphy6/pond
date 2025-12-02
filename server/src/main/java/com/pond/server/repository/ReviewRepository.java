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

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findReviewByReviewerGu(UUID reviewerGu);
    List<Review> findReviewByRevieweeGu(UUID revieweeGu);
    List<Review> findReviewsByTimestamp(LocalDateTime timestamp);

    Optional<Review> findByReviewerGuAndRevieweeGu(UUID reviewerGu, UUID revieweeGu);

    boolean existsByReviewerGuAndRevieweeGu(UUID reviewerGu, UUID revieweeGu);

    // Calculate average rating for a user
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.revieweeGu = :revieweeGu")
    Double findAverageRatingByRevieweeGu(@Param("revieweeGu") UUID revieweeGu);

    Long countByRevieweeGu(UUID revieweeGu);

    // Fetch reviews with reviewer information in a single query
    @Query("SELECT new com.pond.server.dto.ReviewDTO(r.id, r.reviewerGu, r.revieweeGu, r.rating, r.comment, r.timestamp, r.updatedAt, u.username, u.avatar_url) " +
           "FROM Review r JOIN User u ON r.reviewerGu = u.id " +
           "WHERE r.revieweeGu = :revieweeGu")
    List<ReviewDTO> findReviewsWithReviewerInfoByRevieweeGu(@Param("revieweeGu") UUID revieweeGu);

    // Fetch a single review with reviewer information in a single query
    @Query("SELECT new com.pond.server.dto.ReviewDTO(r.id, r.reviewerGu, r.revieweeGu, r.rating, r.comment, r.timestamp, r.updatedAt, u.username, u.avatar_url) " +
           "FROM Review r JOIN User u ON r.reviewerGu = u.id " +
           "WHERE r.id = :reviewId")
    Optional<ReviewDTO> findReviewWithReviewerInfoById(@Param("reviewId") UUID reviewId);
}
