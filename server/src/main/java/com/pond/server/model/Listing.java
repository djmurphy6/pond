package com.pond.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing a marketplace listing in the Pond system.
 * 
 * <p>A listing represents an item for sale created by a user. It contains
 * details about the item including title, description, images, price, and
 * categorization. Listings can be filtered, searched, saved by users, and
 * marked as sold when a transaction is completed.</p>
 * 
 * @author Pond Team
 */
@Entity
@Table(name = "listings")
@Getter
@Setter
public class Listing {
    
    /**
     * Unique identifier for the listing (UUID).
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "listinggu", updatable = false, nullable = false)
    private UUID listingGU;

    /**
     * UUID of the user who created this listing.
     * References the User entity.
     */
    @Column(name = "usergu", nullable = false)
    private UUID userGU;

    /**
     * Detailed description of the item being sold.
     */
    @Column(name = "description")
    private String description;

    /**
     * Title/name of the listing.
     * Displayed prominently in listing cards and search results.
     */
    @Column(name = "title")
    private String title;

    /**
     * URL to the first product image.
     * Primary image displayed in thumbnails.
     */
    @Column(name = "picture1_url")
    private String picture1_url;

    /**
     * URL to the second product image.
     * Optional secondary image for additional detail.
     */
    @Column(name = "picture2_url")
    private String picture2_url;

    /**
     * Price of the item in dollars.
     * Can be null for items listed as "offers" or "free".
     */
    @Column(name = "price")
    private Double price;

    /**
     * Condition of the item (e.g., "New", "Like New", "Good", "Fair").
     */
    @Column(name = "condition")
    private String condition;

    /**
     * Category classification for the listing.
     * Used for filtering and organization (e.g., "Electronics", "Furniture").
     */
    @Column(name = "category")
    private String category;

    /**
     * Timestamp when the listing was created.
     * Set automatically in the constructor.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Flag indicating whether the item has been sold.
     * Defaults to false for new listings.
     */
    @Column(name = "sold", nullable = false)
    private Boolean sold;

    /**
     * UUID of the user who purchased the item.
     * Null until the item is marked as sold.
     */
    @Column(name = "sold_to")
    private UUID soldTo;

    /**
     * Default constructor that initializes createdAt and sold fields.
     */
    public Listing() {
        this.createdAt = LocalDateTime.now();
        this.sold = false;
    }
}
