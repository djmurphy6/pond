package com.pond.server.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pond.server.dto.UpdateUserRequest;
import com.pond.server.dto.UserProfileDTO;
import com.pond.server.model.User;
import com.pond.server.service.UserService;

/**
 * REST controller for user management operations.
 * Handles user profile retrieval, updates, and account deletion.
 */
@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;

    /**
     * Constructs a new UserController with required dependencies.
     *
     * @param userService the service for user operations
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves all users in the system.
     *
     * @return ResponseEntity with list of all users
     */
    @GetMapping("/all")
    public ResponseEntity<?> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves the currently authenticated user's profile.
     *
     * @return ResponseEntity with user profile or 401 if unauthorized
     */
    @GetMapping("/me")
    public ResponseEntity<?> authenticatedUser() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
                return ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
            }
            UserProfileDTO userProfileDTO = new UserProfileDTO(currentUser.getUserGU(), currentUser.getUsername(),
                    currentUser.getEmail(), currentUser.getAvatar_url(), currentUser.getBio(), currentUser.getAdmin());
            return ResponseEntity.ok(userProfileDTO);
    }

    /**
     * Retrieves a user profile by username.
     *
     * @param username the username to look up
     * @return ResponseEntity with user profile or 404 if not found
     */
    @GetMapping("/{username}")
    public ResponseEntity<?> getByUsername(@PathVariable String username) {
            return userService.getProfileByUsername(username).map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        
    }

    /**
     * Updates the currently authenticated user's profile.
     * Can update username and/or bio.
     *
     * @param updateRequest the update request with new profile data
     * @return ResponseEntity with updated profile or error message
     */
    @PutMapping("/me/update")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateUserRequest updateRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        try {
            UserProfileDTO updatedProfile = userService.updateUserProfile(currentUser, updateRequest);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deletes the currently authenticated user's account.
     * Removes all associated data including listings, images, chats, and relationships.
     * Also expires the refresh token cookie (logout).
     *
     * @return ResponseEntity with success message and expired cookie, or error message
     */
    @DeleteMapping("/me")
    public ResponseEntity<?> deleteAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        try {
            userService.deleteAccount(currentUser);
            
            // Clear the refresh token cookie (same as logout)
            ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(true) // for localhost over HTTP use false and sameSite("Lax")
                    .path("/")
                    .maxAge(0)
                    .sameSite("None")
                    .build();
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                    .body(java.util.Map.of("message", "Account deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

}
