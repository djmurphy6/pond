package com.pond.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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

}

