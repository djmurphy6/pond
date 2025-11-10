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

@RestController
@RequestMapping("/following")
public class UserFollowingController {
    
    private final UserFollowingService userFollowingService;
    
    public UserFollowingController(UserFollowingService userFollowingService) {
        this.userFollowingService = userFollowingService;
    }
    
    /**
     * Follow a user
     * POST /following/{userId}
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
     * Unfollow a user
     * DELETE /following/{userId}
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
     * Check if current user follows another user
     * GET /following/{userId}/status
     */
    @GetMapping("/{userId}/status")
    public ResponseEntity<?> checkFollowingStatus(@PathVariable("userId") UUID userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        boolean isFollowing = userFollowingService.isFollowing(currentUser.getUserGU(), userId);
        
        return ResponseEntity.ok(Map.of("following", isFollowing));
    }
    
    /**
     * Get list of users that current user follows
     * GET /following/me
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
     * Get list of users following the current user
     * GET /following/me/followers
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
     * Get follower/following counts for a user
     * GET /following/{userId}/counts
     */
    @GetMapping("/{userId}/counts")
    public ResponseEntity<?> getUserCounts(@PathVariable("userId") UUID userId) {
        return ResponseEntity.ok(Map.of(
            "followers", userFollowingService.getFollowerCount(userId),
            "following", userFollowingService.getFollowingCount(userId)
        ));
    }
}

