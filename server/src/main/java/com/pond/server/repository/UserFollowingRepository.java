package com.pond.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pond.server.model.UserFollowing;

@Repository
public interface UserFollowingRepository extends JpaRepository<UserFollowing, UUID> {
    
    // Check if user A follows user B
    Optional<UserFollowing> findByFollowerGUAndFollowingGU(UUID followerGU, UUID followingGU);
    
    // Get all users that this user follows
    List<UserFollowing> findByFollowerGU(UUID followerGU);
    
    // Get all users following this user (followers)
    List<UserFollowing> findByFollowingGU(UUID followingGU);
    
    // Count followers
    @Query("SELECT COUNT(uf) FROM UserFollowing uf WHERE uf.followingGU = :userGU")
    long countFollowers(@Param("userGU") UUID userGU);
    
    // Count following
    @Query("SELECT COUNT(uf) FROM UserFollowing uf WHERE uf.followerGU = :userGU")
    long countFollowing(@Param("userGU") UUID userGU);
    
    // Check if following (returns boolean directly)
    boolean existsByFollowerGUAndFollowingGU(UUID followerGU, UUID followingGU);
}

