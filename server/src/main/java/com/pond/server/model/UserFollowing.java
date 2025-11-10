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

@Entity
@Table(name = "follows", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"follower_gu", "following_gu"}))
@Getter
@Setter
@NoArgsConstructor
public class UserFollowing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "follower_gu", nullable = false)
    private UUID followerGU;  // User who is following

    @Column(name = "following_gu", nullable = false)
    private UUID followingGU;  // User being followed

    @Column(name = "followed_at", nullable = false)
    private LocalDateTime followedAt;

    public UserFollowing(UUID followerGU, UUID followingGU) {
        this.followerGU = followerGU;
        this.followingGU = followingGU;
        this.followedAt = LocalDateTime.now();
    }
}

