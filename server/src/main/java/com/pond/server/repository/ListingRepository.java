package com.pond.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pond.server.model.Listing;

public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByUserGU(UUID userGU);
    Optional<Listing> findByListingGUAndUserGU(UUID listingGU, UUID userGU);
    
    @Query("SELECT l FROM Listing l WHERE " +
           "(COALESCE(:categories, NULL) IS NULL OR l.category IN :categories) AND " +
           "(:minPrice IS NULL OR l.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.price <= :maxPrice) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'price' AND :sortOrder = 'asc' THEN l.price END ASC, " +
           "CASE WHEN :sortBy = 'price' AND :sortOrder = 'desc' THEN l.price END DESC, " +
           "CASE WHEN :sortBy = 'date' AND :sortOrder = 'asc' THEN l.createdAt END ASC, " +
           "CASE WHEN :sortBy = 'date' AND :sortOrder = 'desc' THEN l.createdAt END DESC")
    List<Listing> findFiltered(
        @Param("categories") List<String> categories,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("sortBy") String sortBy,
        @Param("sortOrder") String sortOrder
    );
}
