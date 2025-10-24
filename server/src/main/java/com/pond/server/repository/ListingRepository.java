package com.pond.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pond.server.model.Listing;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByUserGU(UUID userGU);
    Optional<Listing> findByListingGUAndUserGU(UUID listingGU, UUID userGU);
}
