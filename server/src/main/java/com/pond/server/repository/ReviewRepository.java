package com.pond.server.repository;

import com.pond.server.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
}
