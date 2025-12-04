package com.pond.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a follower/following relationship between users.
 * 
 * <p>Users can follow other users to see their listings in a personalized feed.
 * This creates a directed relationship where one user (follower) follows another
 * user (following). The unique constraint prevents duplicate follow relationships.</p>
 * 
 * @author Pond Team
 */
@Entity
@Table(name = "follows", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"follower_gu", "following_gu"}))
@Getter
@Setter
@NoArgsConstructor
public class UserFollowing {
    
    /**
     * Unique identifier for the follow relationship (UUID).
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * UUID of the user who is following.
     * The subject of the relationship.
     */
    @Column(name = "follower_gu", nullable = false)
    private UUID followerGU;

    /**
     * UUID of the user being followed.
     * The object of the relationship.
     */
    @Column(name = "following_gu", nullable = false)
    private UUID followingGU;

    /**
     * Timestamp when the follow relationship was created.
     * Set automatically in the constructor.
     */
    @Column(name = "followed_at", nullable = false)
    private LocalDateTime followedAt;

    /**
     * Constructs a new UserFollowing relationship.
     * Sets followedAt to current time.
     * 
     * @param followerGU UUID of the user who is following
     * @param followingGU UUID of the user being followed
     */
    public UserFollowing(UUID followerGU, UUID followingGU) {
        this.followerGU = followerGU;
        this.followingGU = followingGU;
        this.followedAt = LocalDateTime.now();
    }
}

