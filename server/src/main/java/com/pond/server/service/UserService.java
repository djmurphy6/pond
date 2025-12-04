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

/**
 * Service class for managing user-related operations.
 * Handles user profile management, account updates, and account deletion with cascade cleanup.
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ListingService listingService;
    private final SupabaseStorage supabaseStorage;
    
    @Value("${supabase.pfp-bucket}")
    private String pfpBucket;
    
    /**
     * Constructs a new UserService with required dependencies.
     *
     * @param userRepository the repository for user data access
     * @param listingRepository the repository for listing data access
     * @param listingService the service for listing operations
     * @param supabaseStorage the service for Supabase storage operations
     */
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

    /**
     * Retrieves all users from the database.
     *
     * @return a list of all users
     */
    @Transactional(readOnly = true)
    public List<User> allUsers(){
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }
    
    /**
     * Retrieves a user profile by username.
     *
     * @param username the username to search for
     * @return an Optional containing the UserProfileDTO if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<UserProfileDTO> getProfileByUsername(String username){
        return userRepository.findByUsername(username).map(u -> new UserProfileDTO(u.getUserGU(), u.getUsername(), u.getEmail(), u.getAvatar_url(), u.getBio(), u.getAdmin()));
    }
    
    /**
     * Finds a user by username or email identifier.
     * This method is primarily used for authentication and WebSocket connection establishment.
     *
     * @param identifier the username or email to search for
     * @return an Optional containing the User if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsernameOrEmail(String identifier) {
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier));
    }
    
    /**
     * Finds a user by their unique identifier.
     *
     * @param userGU the unique UUID of the user
     * @return an Optional containing the User if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID userGU) {
        return userRepository.findById(userGU);
    }

    /**
     * Updates a user's profile with the provided information.
     * Validates username uniqueness and updates username and bio if provided.
     *
     * @param user the user entity to update
     * @param updateRequest the update request containing new username and/or bio
     * @return the updated UserProfileDTO
     * @throws RuntimeException if the requested username is already taken by another user
     */
    @Transactional
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
    
    /**
     * Updates the user's avatar URL in the database.
     *
     * @param user the user entity to update
     * @param avatarUrl the new avatar URL
     * @return the updated User entity
     */
    @Transactional
    public User updateAvatar(User user, String avatarUrl) {
        user.setAvatar_url(avatarUrl);
        return userRepository.save(user);
    }

    /**
     * Deletes a user account and all associated data.
     * This includes:
     * - User's avatar from Supabase storage
     * - All listings owned by the user (with their images)
     * - Chat rooms, messages, saved listings, and following relationships (via database CASCADE)
     * Note: Reports are preserved for record-keeping purposes.
     *
     * @param user the user entity to delete
     */
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

    /**
     * Deletes a user's avatar from Supabase storage.
     * Extracts the storage key from the avatar URL and performs deletion.
     * Failure to delete the avatar does not prevent the rest of the account deletion.
     *
     * @param user the user whose avatar should be deleted
     */
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

