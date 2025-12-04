package com.pond.server.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pond.server.dto.ListingDTO;
import com.pond.server.model.Listing;
import com.pond.server.model.SavedListing;
import com.pond.server.model.User;
import com.pond.server.repository.ListingRepository;
import com.pond.server.repository.SavedListingRepository;

/**
 * Service class for managing saved listings.
 * Handles saving/unsaving listings and retrieving user's saved listings.
 */
@Service
public class SavedListingService {
    
    private final SavedListingRepository savedListingRepository;
    private final ListingRepository listingRepository;
    
    /**
     * Constructs a new SavedListingService with required dependencies.
     *
     * @param savedListingRepository the repository for saved listing data access
     * @param listingRepository the repository for listing data access
     */
    public SavedListingService(SavedListingRepository savedListingRepository, 
                               ListingRepository listingRepository) {
        this.savedListingRepository = savedListingRepository;
        this.listingRepository = listingRepository;
    }
    
    /**
     * Saves a listing for a user (adds to favorites).
     * Prevents duplicate saves.
     *
     * @param listingGU the UUID of the listing to save
     * @param user the user saving the listing
     * @throws RuntimeException if listing not found or already saved
     */
    @Transactional
    public void saveListing(UUID listingGU, User user) {
        // Check if listing exists
        listingRepository.findById(listingGU)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        
        // Check if already saved
        if (savedListingRepository.existsByUserGUAndListingGU(user.getUserGU(), listingGU)) {
            throw new RuntimeException("Listing already saved");
        }
        
        // Create and save
        SavedListing savedListing = new SavedListing(user.getUserGU(), listingGU);
        savedListingRepository.save(savedListing);
    }
    
    /**
     * Removes a listing from user's saved listings (removes from favorites).
     *
     * @param listingGU the UUID of the listing to unsave
     * @param user the user unsaving the listing
     * @throws RuntimeException if saved listing relationship not found
     */
    @Transactional
    public void unsaveListing(UUID listingGU, User user) {
        SavedListing savedListing = savedListingRepository
                .findByUserGUAndListingGU(user.getUserGU(), listingGU)
                .orElseThrow(() -> new RuntimeException("Saved listing not found"));
        
        savedListingRepository.delete(savedListing);
    }
    
    /**
     * Checks if a specific listing is saved by a user.
     *
     * @param listingGU the UUID of the listing to check
     * @param user the user to check for
     * @return true if the listing is saved by the user, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isListingSaved(UUID listingGU, User user) {
        return savedListingRepository.existsByUserGUAndListingGU(user.getUserGU(), listingGU);
    }
    
    /**
     * Retrieves all saved listings for a user with full listing details.
     * OPTIMIZED: Uses single JOIN query instead of fetching separately and filtering.
     *
     * @param user the user whose saved listings to retrieve
     * @return a list of saved listings as ListingDTOs (sorted by save date, newest first)
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getSavedListings(User user) {
        // Single query with JOIN - much faster than fetching separately
        List<Listing> listings = savedListingRepository
                .findSavedListingsWithDetails(user.getUserGU());
        
        // Convert to DTOs (already sorted by savedAt DESC from query)
        return listings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves a list of listing IDs that a user has saved.
     * Lightweight method for checking save status of multiple listings.
     *
     * @param user the user whose saved listing IDs to retrieve
     * @return a list of UUIDs of saved listings
     */
    @Transactional(readOnly = true)
    public List<UUID> getSavedListingIds(User user) {
        return savedListingRepository.findListingGUsByUserGU(user.getUserGU());
    }
    
    /**
     * Converts a Listing entity to a ListingDTO.
     *
     * @param l the listing entity to convert
     * @return the ListingDTO representation
     */
    private ListingDTO toDto(Listing l) {
        return new ListingDTO(
            l.getListingGU(),
            l.getUserGU(),
            l.getTitle(),
            l.getDescription(),
            l.getPicture1_url(),
            l.getPicture2_url(),
            l.getPrice(),
            l.getCondition(),
            l.getCategory(),
            l.getCreatedAt(),
            l.getSold(),
            l.getSoldTo()
        );
    }
}

