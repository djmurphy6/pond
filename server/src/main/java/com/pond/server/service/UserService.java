package com.pond.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pond.server.dto.UpdateUserRequest;
import com.pond.server.dto.UserProfileDTO;
import com.pond.server.model.Listing;
import com.pond.server.model.User;
import com.pond.server.repository.ListingRepository;
import com.pond.server.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ListingService listingService;
    private final SupabaseStorage supabaseStorage;
    
    @Value("${supabase.pfp-bucket}")
    private String pfpBucket;
    
    public UserService(
            UserRepository userRepository,
            ListingRepository listingRepository,
            ListingService listingService,
            SupabaseStorage supabaseStorage
    ) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.listingService = listingService;
        this.supabaseStorage = supabaseStorage;
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

    @Transactional
    public void deleteAccount(User user) {
        UUID userGU = user.getUserGU();
        
        System.out.println("Starting account deletion for user: " + userGU);
        
        // 1. Delete user's avatar from Supabase storage
        deleteUserAvatar(user);
        
        // 2. Delete all listings owned by the user (this handles Supabase image deletion)
        List<Listing> userListings = listingRepository.findByUserGU(userGU);
        System.out.println("Found " + userListings.size() + " listings to delete for user: " + userGU);
        for (Listing listing : userListings) {
            System.out.println("Deleting listing: " + listing.getListingGU());
            listingService.delete(listing.getListingGU(), user);
        }
        
        // 3. Delete the user - database CASCADE will automatically delete:
        //    - Chat rooms (via seller_gu/buyer_gu foreign keys)
        //    - Messages (via sender_gu foreign key)
        //    - Saved listings (via user_gu foreign key)
        //    - User following relationships (via follower_gu/following_gu foreign keys)
        // Note: Reports are kept for record-keeping purposes
        System.out.println("Deleting user from database: " + userGU);
        userRepository.delete(user);
        System.out.println("Account deletion completed for user: " + userGU);
    }

    private void deleteUserAvatar(User user) {
        String avatarUrl = user.getAvatar_url();
        if (avatarUrl == null || avatarUrl.isBlank()) {
            System.out.println("No avatar to delete for user: " + user.getUserGU());
            return;
        }
        
        System.out.println("Attempting to delete avatar: " + avatarUrl);
        
        // Extract the key from the avatar URL
        // Format: {storageUrl}/storage/v1/object/public/{bucket}/{userGU}/{uuid}.jpg
        String marker = "/storage/v1/object/public/" + pfpBucket + "/";
        int idx = avatarUrl.indexOf(marker);
        if (idx >= 0) {
            String key = avatarUrl.substring(idx + marker.length());
            System.out.println("Extracted avatar key: " + key + " from bucket: " + pfpBucket);
            try {
                supabaseStorage.deleteObject(pfpBucket, key);
                System.out.println("Successfully deleted avatar from storage");
            } catch (Exception e) {
                // Log but don't fail the entire operation if avatar deletion fails
                System.err.println("ERROR: Failed to delete avatar " + avatarUrl);
                System.err.println("Key: " + key);
                System.err.println("Bucket: " + pfpBucket);
                System.err.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("WARNING: Could not extract key from avatar URL: " + avatarUrl);
            System.err.println("Expected marker: " + marker);
        }
    }

}

