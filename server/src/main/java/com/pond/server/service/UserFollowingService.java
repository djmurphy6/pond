package com.pond.server.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pond.server.model.UserFollowing;
import com.pond.server.repository.UserFollowingRepository;
import com.pond.server.repository.UserRepository;

@Service
public class UserFollowingService {
    
    private final UserFollowingRepository userFollowingRepository;
    private final UserRepository userRepository;
    
    public UserFollowingService(UserFollowingRepository userFollowingRepository,
                               UserRepository userRepository) {
        this.userFollowingRepository = userFollowingRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Follow a user
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
     * Unfollow a user
     */
    @Transactional
    public void unfollowUser(UUID followerGU, UUID followingGU) {
        UserFollowing following = userFollowingRepository
            .findByFollowerGUAndFollowingGU(followerGU, followingGU)
            .orElseThrow(() -> new IllegalArgumentException("Not following this user"));
        
        userFollowingRepository.delete(following);
    }
    
    /**
     * Check if user A follows user B
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(UUID followerGU, UUID followingGU) {
        return userFollowingRepository.existsByFollowerGUAndFollowingGU(followerGU, followingGU);
    }
    
    /**
     * Get list of user GUIDs that this user follows
     */
    @Transactional(readOnly = true)
    public List<UUID> getFollowingList(UUID userGU) {
        return userFollowingRepository.findByFollowerGU(userGU).stream()
            .map(UserFollowing::getFollowingGU)
            .collect(Collectors.toList());
    }
    
    /**
     * Get list of user GUIDs who follow this user
     */
    @Transactional(readOnly = true)
    public List<UUID> getFollowersList(UUID userGU) {
        return userFollowingRepository.findByFollowingGU(userGU).stream()
            .map(UserFollowing::getFollowerGU)
            .collect(Collectors.toList());
    }
    
    /**
     * Get follower count
     */
    @Transactional(readOnly = true)
    public long getFollowerCount(UUID userGU) {
        return userFollowingRepository.countFollowers(userGU);
    }
    
    /**
     * Get following count
     */
    @Transactional(readOnly = true)
    public long getFollowingCount(UUID userGU) {
        return userFollowingRepository.countFollowing(userGU);
    }
}

