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
    
    // OPTIMIZED: Uses database indexes and limits result set for better performance
    @Query("SELECT l FROM Listing l WHERE " +
           "(COALESCE(:categories, NULL) IS NULL OR l.category IN :categories) AND " +
           "(:minPrice IS NULL OR l.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.price <= :maxPrice) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'price' AND :sortOrder = 'asc' THEN l.price END ASC NULLS LAST, " +
           "CASE WHEN :sortBy = 'price' AND :sortOrder = 'desc' THEN l.price END DESC NULLS LAST, " +
           "CASE WHEN :sortBy = 'date' AND :sortOrder = 'asc' THEN l.createdAt END ASC NULLS LAST, " +
           "CASE WHEN :sortBy = 'date' AND :sortOrder = 'desc' THEN l.createdAt END DESC NULLS LAST")
    List<Listing> findFilteredWithLimit(
        @Param("categories") List<String> categories,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("sortBy") String sortBy,
        @Param("sortOrder") String sortOrder,
        @Param("searchQuery") String searchQuery,
        org.springframework.data.domain.Pageable pageable
    );
    
    // Keep original method for backward compatibility
    default List<Listing> findFiltered(List<String> categories, Double minPrice, Double maxPrice, 
                                       String sortBy, String sortOrder, String searchQuery) {
        return findFilteredWithLimit(categories, minPrice, maxPrice, sortBy, sortOrder, searchQuery,
                                    org.springframework.data.domain.PageRequest.of(0, 500));
    }
    
    // OPTIMIZED: Query for following listings with limit - prevents N+1 query problem
    @Query("SELECT l FROM Listing l WHERE " +
           "l.userGU IN :userIds AND " +
           "(COALESCE(:categories, NULL) IS NULL OR l.category IN :categories) AND " +
           "(:minPrice IS NULL OR l.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.price <= :maxPrice) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'price' AND :sortOrder = 'asc' THEN l.price END ASC NULLS LAST, " +
           "CASE WHEN :sortBy = 'price' AND :sortOrder = 'desc' THEN l.price END DESC NULLS LAST, " +
           "CASE WHEN :sortBy = 'date' AND :sortOrder = 'asc' THEN l.createdAt END ASC NULLS LAST, " +
           "CASE WHEN :sortBy = 'date' AND :sortOrder = 'desc' THEN l.createdAt END DESC NULLS LAST")
    List<Listing> findFollowingFilteredWithLimit(
        @Param("userIds") List<UUID> userIds,
        @Param("categories") List<String> categories,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("sortBy") String sortBy,
        @Param("sortOrder") String sortOrder,
        org.springframework.data.domain.Pageable pageable
    );
    
    // Keep original method for backward compatibility
    default List<Listing> findFollowingFiltered(List<UUID> userIds, List<String> categories, 
                                                 Double minPrice, Double maxPrice, 
                                                 String sortBy, String sortOrder) {
        return findFollowingFilteredWithLimit(userIds, categories, minPrice, maxPrice, sortBy, sortOrder,
                                             org.springframework.data.domain.PageRequest.of(0, 500));
    }
}
