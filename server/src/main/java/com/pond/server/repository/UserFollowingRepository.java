package com.pond.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pond.server.model.UserFollowing;

/**
 * Repository interface for {@link UserFollowing} entity database operations.
 * 
 * <p>Manages follower/following relationships between users. Supports social
 * features like following sellers to see their listings in a personalized feed.
 * Provides methods for relationship queries, counts, and existence checks.</p>
 * 
 * @author Pond Team
 * @see UserFollowing
 */
@Repository
public interface UserFollowingRepository extends JpaRepository<UserFollowing, UUID> {
    
    /**
     * Finds a follow relationship between two users.
     * Used to check if user A follows user B.
     * 
     * @param followerGU UUID of the follower
     * @param followingGU UUID of the user being followed
     * @return an Optional containing the relationship if it exists
     */
    Optional<UserFollowing> findByFollowerGUAndFollowingGU(UUID followerGU, UUID followingGU);
    
    /**
     * Gets all users that this user follows (following list).
     * 
     * @param followerGU UUID of the follower
     * @return list of UserFollowing relationships for users being followed
     */
    List<UserFollowing> findByFollowerGU(UUID followerGU);
    
    /**
     * Gets all users following this user (followers list).
     * 
     * @param followingGU UUID of the user being followed
     * @return list of UserFollowing relationships for followers
     */
    List<UserFollowing> findByFollowingGU(UUID followingGU);
    
    /**
     * Counts how many followers a user has.
     * Used to display follower count on user profiles.
     * 
     * @param userGU UUID of the user
     * @return count of followers
     */
    @Query("SELECT COUNT(uf) FROM UserFollowing uf WHERE uf.followingGU = :userGU")
    long countFollowers(@Param("userGU") UUID userGU);
    
    /**
     * Counts how many users this user is following.
     * Used to display following count on user profiles.
     * 
     * @param userGU UUID of the user
     * @return count of users being followed
     */
    @Query("SELECT COUNT(uf) FROM UserFollowing uf WHERE uf.followerGU = :userGU")
    long countFollowing(@Param("userGU") UUID userGU);
    
    /**
     * Checks if a follow relationship exists.
     * Used to show follow/unfollow button state in UI.
     * 
     * @param followerGU UUID of the follower
     * @param followingGU UUID of the user being followed
     * @return true if the relationship exists, false otherwise
     */
    boolean existsByFollowerGUAndFollowingGU(UUID followerGU, UUID followingGU);
}

