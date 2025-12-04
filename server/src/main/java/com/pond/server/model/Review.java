package com.pond.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a user review in the Pond marketplace.
 * 
 * <p>Reviews allow users to rate and comment on their transaction experiences
 * with other users. Each review includes a numerical rating (typically 1-5)
 * and an optional comment. Users can only review others they've completed
 * transactions with.</p>
 * 
 * <p>Reviews can be edited, as indicated by the {@link #updatedAt} timestamp.</p>
 * 
 * @author Pond Team
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@AllArgsConstructor
public class Review {

    /**
     * Unique identifier for the review (UUID).
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * UUID of the user who wrote the review.
     */
    @Column(name ="reviewer_gu", nullable = false)
    private UUID reviewerGu;

    /**
     * UUID of the user being reviewed.
     * The review affects this user's overall rating.
     */
    @Column(name ="reviewee_gu", nullable = false)
    private UUID revieweeGu;

    /**
     * Numerical rating given in the review.
     * Typically on a scale of 1-5, where 5 is the best.
     */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    /**
     * Written comment accompanying the review.
     * Maximum length of 500 characters.
     * Can be updated after initial submission.
     */
    @Column(name = "comment", updatable = true, nullable = false, columnDefinition = "VARCHAR(500)")
    private String comment;

    /**
     * Timestamp when the review was originally created.
     * Set automatically in the constructor.
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Timestamp of the last update to the review.
     * Null if the review has never been edited.
     */
    @Column(name="updatedAt")
    private LocalDateTime updatedAt;

    /**
     * Default constructor that initializes the timestamp.
     */
    public Review(){
        this.timestamp = LocalDateTime.now();
    }

}
