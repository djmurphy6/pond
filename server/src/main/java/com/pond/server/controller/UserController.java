package com.pond.server.controller;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> allUsers(){
        logger.info("allUsers() endpoint called");
        List<User> users = userService.allUsers();
        logger.info("Returning {} users", users.size());
        return ResponseEntity.ok(users);
    }



    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> authenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        UserProfileDTO userProfileDTO = new UserProfileDTO(currentUser.getId(), currentUser.getUsername(), currentUser.getEmail(), currentUser.getAvatar_url());
        return ResponseEntity.ok(userProfileDTO);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileDTO> getByUsername(@PathVariable String username){
        return userService.getProfileByUsername(username).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

}
