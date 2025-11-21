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

@Service
public class SavedListingService {
    
    private final SavedListingRepository savedListingRepository;
    private final ListingRepository listingRepository;
    
    public SavedListingService(SavedListingRepository savedListingRepository, 
                               ListingRepository listingRepository) {
        this.savedListingRepository = savedListingRepository;
        this.listingRepository = listingRepository;
    }
    
    /**
     * Save a listing for a user
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
     * Unsave a listing for a user
     */
    @Transactional
    public void unsaveListing(UUID listingGU, User user) {
        SavedListing savedListing = savedListingRepository
                .findByUserGUAndListingGU(user.getUserGU(), listingGU)
                .orElseThrow(() -> new RuntimeException("Saved listing not found"));
        
        savedListingRepository.delete(savedListing);
    }
    
    /**
     * Check if a listing is saved by a user
     */
    public boolean isListingSaved(UUID listingGU, User user) {
        return savedListingRepository.existsByUserGUAndListingGU(user.getUserGU(), listingGU);
    }
    
    /**
     * Get all saved listings for a user (returns full listing details)
     * OPTIMIZED: Uses single JOIN query instead of fetching separately and filtering
     */
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
     * Get list of listing IDs that user has saved (lightweight, for checking status)
     */
    public List<UUID> getSavedListingIds(User user) {
        return savedListingRepository.findListingGUsByUserGU(user.getUserGU());
    }
    
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

