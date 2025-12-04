package com.pond.server.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pond.server.model.User;
import com.pond.server.service.UserFollowingService;

/**
 * REST controller for user following relationship management.
 * Handles follow/unfollow operations and retrieval of followers/following lists.
 */
@RestController
@RequestMapping("/following")
public class UserFollowingController {
    
    private final UserFollowingService userFollowingService;
    
    /**
     * Constructs a new UserFollowingController with required dependencies.
     *
     * @param userFollowingService the service for user following operations
     */
    public UserFollowingController(UserFollowingService userFollowingService) {
        this.userFollowingService = userFollowingService;
    }
    
    /**
     * Creates a following relationship between the authenticated user and another user.
     *
     * @param userIdToFollow the UUID of the user to follow
     * @return ResponseEntity with success message or error
     */
    @PostMapping("/{userId}")
    public ResponseEntity<?> followUser(@PathVariable("userId") UUID userIdToFollow) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            
            userFollowingService.followUser(currentUser.getUserGU(), userIdToFollow);
            
            return ResponseEntity.ok(Map.of(
                "message", "Successfully followed user",
                "following", true
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Removes a following relationship between the authenticated user and another user.
     *
     * @param userIdToUnfollow the UUID of the user to unfollow
     * @return ResponseEntity with success message or error
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable("userId") UUID userIdToUnfollow) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            
            userFollowingService.unfollowUser(currentUser.getUserGU(), userIdToUnfollow);
            
            return ResponseEntity.ok(Map.of(
                "message", "Successfully unfollowed user",
                "following", false
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Checks if the authenticated user follows another user.
     *
     * @param userId the UUID of the user to check
     * @return ResponseEntity with following status
     */
    @GetMapping("/{userId}/status")
    public ResponseEntity<?> checkFollowingStatus(@PathVariable("userId") UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        boolean isFollowing = userFollowingService.isFollowing(currentUser.getUserGU(), userId);
        
        return ResponseEntity.ok(Map.of("following", isFollowing));
    }
    
    /**
     * Retrieves a list of users that the authenticated user follows.
     *
     * @return ResponseEntity with list of following user UUIDs
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyFollowing() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        return ResponseEntity.ok(Map.of(
            "following", userFollowingService.getFollowingList(currentUser.getUserGU())
        ));
    }
    
    /**
     * Retrieves a list of users following the authenticated user.
     *
     * @return ResponseEntity with list of follower user UUIDs
     */
    @GetMapping("/me/followers")
    public ResponseEntity<?> getMyFollowers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        return ResponseEntity.ok(Map.of(
            "followers", userFollowingService.getFollowersList(currentUser.getUserGU())
        ));
    }
    
    /**
     * Retrieves follower and following counts for a specific user.
     *
     * @param userId the UUID of the user
     * @return ResponseEntity with follower and following counts
     */
    @GetMapping("/{userId}/counts")
    public ResponseEntity<?> getUserCounts(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(Map.of(
            "followers", userFollowingService.getFollowerCount(userId),
            "following", userFollowingService.getFollowingCount(userId)
        ));
    }
}

