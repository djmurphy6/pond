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

@Entity
@Table(name = "saved_listings", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_gu", "listing_gu"}))
@Getter
@Setter
@NoArgsConstructor
public class SavedListing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_gu", nullable = false)
    private UUID userGU;

    @Column(name = "listing_gu", nullable = false)
    private UUID listingGU;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    public SavedListing(UUID userGU, UUID listingGU) {
        this.userGU = userGU;
        this.listingGU = listingGU;
        this.savedAt = LocalDateTime.now();
    }
}

