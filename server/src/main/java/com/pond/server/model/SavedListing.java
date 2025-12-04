package com.pond.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a user's saved/bookmarked listing.
 * 
 * <p>Users can save listings they're interested in for quick access later.
 * This creates a many-to-many relationship between users and listings.
 * The unique constraint ensures a user cannot save the same listing twice.</p>
 * 
 * @author Pond Team
 */
@Entity
@Table(name = "saved_listings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_gu", "listing_gu"}))
@Getter
@Setter
@NoArgsConstructor
public class SavedListing {
    
    /**
     * Unique identifier for the saved listing record (UUID).
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * UUID of the user who saved the listing.
     */
    @Column(name = "user_gu", nullable = false)
    private UUID userGU;

    /**
     * UUID of the listing that was saved.
     */
    @Column(name = "listing_gu", nullable = false)
    private UUID listingGU;

    /**
     * Timestamp when the listing was saved.
     * Used for ordering saved listings chronologically.
     */
    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    /**
     * Constructs a new SavedListing with the specified user and listing.
     * Sets savedAt to current time.
     * 
     * @param userGU UUID of the user saving the listing
     * @param listingGU UUID of the listing being saved
     */
    public SavedListing(UUID userGU, UUID listingGU) {
        this.userGU = userGU;
        this.listingGU = listingGU;
        this.savedAt = LocalDateTime.now();
    }
}

