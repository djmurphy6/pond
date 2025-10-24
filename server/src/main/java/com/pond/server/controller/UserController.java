package com.pond.server.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
