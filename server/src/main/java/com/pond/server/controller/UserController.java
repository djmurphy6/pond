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

@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

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

    @GetMapping("/{username}")
    public ResponseEntity<?> getByUsername(@PathVariable String username) {
            return userService.getProfileByUsername(username).map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        
    }

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
