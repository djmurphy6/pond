package com.pond.server.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pond.server.model.UserFollowing;
import com.pond.server.repository.UserFollowingRepository;
import com.pond.server.repository.UserRepository;

/**
 * Service class for managing user following relationships.
 * Handles follow/unfollow operations, relationship checks, and follower/following counts.
 */
@Service
public class UserFollowingService {
    
    private final UserFollowingRepository userFollowingRepository;
    private final UserRepository userRepository;
    
    /**
     * Constructs a new UserFollowingService with required dependencies.
     *
     * @param userFollowingRepository the repository for user following data access
     * @param userRepository the repository for user data access
     */
    public UserFollowingService(UserFollowingRepository userFollowingRepository,
                               UserRepository userRepository) {
        this.userFollowingRepository = userFollowingRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Creates a following relationship between two users.
     * Prevents self-following, following non-existent users, and duplicate follows.
     *
     * @param followerGU the UUID of the user initiating the follow
     * @param followingGU the UUID of the user being followed
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public void followUser(UUID followerGU, UUID followingGU) {
        // Prevent self-following
        if (followerGU.equals(followingGU)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        
        // Check if user to follow exists
        if (!userRepository.existsById(followingGU)) {
            throw new IllegalArgumentException("User to follow does not exist");
        }
        
        // Check if already following
        if (userFollowingRepository.existsByFollowerGUAndFollowingGU(followerGU, followingGU)) {
            throw new IllegalArgumentException("Already following this user");
        }
        
        UserFollowing following = new UserFollowing(followerGU, followingGU);
        userFollowingRepository.save(following);
    }
    
    /**
     * Removes a following relationship between two users.
     *
     * @param followerGU the UUID of the user unfollowing
     * @param followingGU the UUID of the user being unfollowed
     * @throws IllegalArgumentException if following relationship doesn't exist
     */
    @Transactional
    public void unfollowUser(UUID followerGU, UUID followingGU) {
        UserFollowing following = userFollowingRepository
            .findByFollowerGUAndFollowingGU(followerGU, followingGU)
            .orElseThrow(() -> new IllegalArgumentException("Not following this user"));
        
        userFollowingRepository.delete(following);
    }
    
    /**
     * Checks if one user follows another user.
     *
     * @param followerGU the UUID of the potential follower
     * @param followingGU the UUID of the potential followee
     * @return true if follower follows followee, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(UUID followerGU, UUID followingGU) {
        return userFollowingRepository.existsByFollowerGUAndFollowingGU(followerGU, followingGU);
    }
    
    /**
     * Retrieves a list of user UUIDs that a specific user follows.
     *
     * @param userGU the UUID of the user
     * @return a list of UUIDs of users being followed
     */
    @Transactional(readOnly = true)
    public List<UUID> getFollowingList(UUID userGU) {
        return userFollowingRepository.findByFollowerGU(userGU).stream()
            .map(UserFollowing::getFollowingGU)
            .collect(Collectors.toList());
    }
    
    /**
     * Retrieves a list of user UUIDs who follow a specific user.
     *
     * @param userGU the UUID of the user
     * @return a list of UUIDs of followers
     */
    @Transactional(readOnly = true)
    public List<UUID> getFollowersList(UUID userGU) {
        return userFollowingRepository.findByFollowingGU(userGU).stream()
            .map(UserFollowing::getFollowerGU)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets the count of followers for a user.
     *
     * @param userGU the UUID of the user
     * @return the number of followers
     */
    @Transactional(readOnly = true)
    public long getFollowerCount(UUID userGU) {
        return userFollowingRepository.countFollowers(userGU);
    }
    
    /**
     * Gets the count of users that a specific user is following.
     *
     * @param userGU the UUID of the user
     * @return the number of users being followed
     */
    @Transactional(readOnly = true)
    public long getFollowingCount(UUID userGU) {
        return userFollowingRepository.countFollowing(userGU);
    }
}

