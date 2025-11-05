package com.pond.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pond.server.dto.UpdateUserRequest;
import com.pond.server.dto.UserProfileDTO;
import com.pond.server.model.User;
import com.pond.server.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public List<User> allUsers(){
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }
    public Optional<UserProfileDTO> getProfileByUsername(String username){
        return userRepository.findByUsername(username).map(u -> new UserProfileDTO(u.getUserGU(), u.getUsername(), u.getEmail(), u.getAvatar_url(), u.getBio(), u.getAdmin()));
    }

    public UserProfileDTO updateUserProfile(User user, UpdateUserRequest updateRequest) {
        // Update username if provided and not blank
        if (updateRequest.getUsername() != null && !updateRequest.getUsername().isBlank()) {
            // Check if username is already taken by another user
            Optional<User> existingUser = userRepository.findByUsername(updateRequest.getUsername());
            if (existingUser.isPresent() && !existingUser.get().getUserGU().equals(user.getUserGU())) {
                throw new RuntimeException("Username already taken");
            }
            user.setUsername(updateRequest.getUsername());
        }

        // Update bio if provided (can be null or blank to clear it)
        if (updateRequest.getBio() != null) {
            user.setBio(updateRequest.getBio());
        }

        User savedUser = userRepository.save(user);
        return new UserProfileDTO(
            savedUser.getUserGU(), 
            savedUser.getUsername(), 
            savedUser.getEmail(), 
            savedUser.getAvatar_url(), 
            savedUser.getBio(), 
            savedUser.getAdmin()
        );
    }

}

