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

/**
 * Repository interface for {@link SavedListing} entity database operations.
 * 
 * <p>Manages user bookmarks/saved listings, allowing users to save listings for
 * later viewing. Includes optimized JOIN queries to fetch full listing details
 * without N+1 query problems.</p>
 * 
 * @author Pond Team
 * @see SavedListing
 * @see Listing
 */
@Repository
public interface SavedListingRepository extends JpaRepository<SavedListing, UUID> {
    
    /**
     * Finds a specific saved listing record by user and listing.
     * 
     * @param userGU UUID of the user
     * @param listingGU UUID of the listing
     * @return an Optional containing the saved listing if found
     */
    Optional<SavedListing> findByUserGUAndListingGU(UUID userGU, UUID listingGU);
    
    /**
     * Gets all saved listings for a user, ordered by save date (newest first).
     * 
     * @param userGU UUID of the user
     * @return list of saved listings ordered by savedAt descending
     */
    List<SavedListing> findByUserGUOrderBySavedAtDesc(UUID userGU);
    
    /**
     * Checks if a specific listing is saved by a user.
     * Used to show save/unsave button state in UI.
     * 
     * @param userGU UUID of the user
     * @param listingGU UUID of the listing
     * @return true if the listing is saved by the user, false otherwise
     */
    boolean existsByUserGUAndListingGU(UUID userGU, UUID listingGU);
    
    /**
     * Deletes a saved listing record (unsaves a listing).
     * 
     * @param userGU UUID of the user
     * @param listingGU UUID of the listing to unsave
     */
    void deleteByUserGUAndListingGU(UUID userGU, UUID listingGU);
    
    /**
     * Gets a list of listing UUIDs that a user has saved.
     * Useful for bulk checking save status across multiple listings.
     * 
     * @param userGU UUID of the user
     * @return list of saved listing UUIDs
     */
    @Query("SELECT s.listingGU FROM SavedListing s WHERE s.userGU = :userGU")
    List<UUID> findListingGUsByUserGU(@Param("userGU") UUID userGU);
    
    /**
     * Gets saved listings with full listing details using JOIN query.
     * OPTIMIZED: Prevents N+1 query problem by fetching listings in one query.
     * Results are ordered by when they were saved (newest first).
     * 
     * @param userGU UUID of the user
     * @return list of full Listing entities that the user has saved
     */
    @Query("SELECT l FROM Listing l " +
           "JOIN SavedListing s ON l.listingGU = s.listingGU " +
           "WHERE s.userGU = :userGU " +
           "ORDER BY s.savedAt DESC")
    List<Listing> findSavedListingsWithDetails(@Param("userGU") UUID userGU);
}

