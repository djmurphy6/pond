package com.pond.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pond.server.model.Listing;
import com.pond.server.model.SavedListing;

@Repository
public interface SavedListingRepository extends JpaRepository<SavedListing, UUID> {
    
    // Find a specific saved listing by user and listing
    Optional<SavedListing> findByUserGUAndListingGU(UUID userGU, UUID listingGU);
    
    // Get all saved listings for a user
    List<SavedListing> findByUserGUOrderBySavedAtDesc(UUID userGU);
    
    // Check if a listing is saved by a user
    boolean existsByUserGUAndListingGU(UUID userGU, UUID listingGU);
    
    // Delete a saved listing
    void deleteByUserGUAndListingGU(UUID userGU, UUID listingGU);
    
    // Get list of listing GUIDs that user has saved (useful for bulk checking)
    @Query("SELECT s.listingGU FROM SavedListing s WHERE s.userGU = :userGU")
    List<UUID> findListingGUsByUserGU(@Param("userGU") UUID userGU);
    
    // OPTIMIZED: Get saved listings with full listing details in a single JOIN query
    @Query("SELECT l FROM Listing l " +
           "JOIN SavedListing s ON l.listingGU = s.listingGU " +
           "WHERE s.userGU = :userGU " +
           "ORDER BY s.savedAt DESC")
    List<Listing> findSavedListingsWithDetails(@Param("userGU") UUID userGU);
}

