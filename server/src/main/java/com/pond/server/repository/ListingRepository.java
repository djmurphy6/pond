package com.pond.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pond.server.dto.ListingDTO;
import com.pond.server.model.Listing;

/**
 * Repository interface for {@link Listing} entity database operations.
 * 
 * <p>Provides comprehensive querying capabilities for marketplace listings including
 * filtering, sorting, pagination, and optimized DTO projections to avoid N+1 query
 * problems. Supports both entity and DTO return types for performance optimization.</p>
 * 
 * <p>Key features include:</p>
 * <ul>
 *   <li>Filtered searches by category, price range, and date</li>
 *   <li>Personalized feeds showing listings from followed users</li>
 *   <li>Transaction verification for review eligibility</li>
 *   <li>DTO projections for improved query performance</li>
 * </ul>
 * 
 * @author Pond Team
 * @see Listing
 * @see com.pond.server.dto.ListingDTO
 */
public interface ListingRepository extends JpaRepository<Listing, UUID> {
    
    /**
     * Finds all listings created by a specific user.
     * 
     * @param userGU the UUID of the user
     * @return list of listings owned by the user
     */
    List<Listing> findByUserGU(UUID userGU);
    
    /**
     * Finds a specific listing owned by a specific user.
     * Used to verify listing ownership before edit/delete operations.
     * 
     * @param listingGU the UUID of the listing
     * @param userGU the UUID of the user
     * @return an Optional containing the listing if found and owned by user
     */
    Optional<Listing> findByListingGUAndUserGU(UUID listingGU, UUID userGU);
    

    /**
     * Finds listings matching filter criteria with pagination support.
     * Returns full Listing entities.
     * 
     * @param categories list of category names to filter by (null for all)
     * @param minPrice minimum price threshold (null for no minimum)
     * @param maxPrice maximum price threshold (null for no maximum)
     * @param sortBy field to sort by ("price" or "date")
     * @param sortOrder sort direction ("asc" or "desc")
     * @param searchQuery search text (currently unused in query)
     * @param pageable pagination parameters
     * @return list of matching listings
     */
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
    
    /**
     * Finds listings matching filter criteria using DTO projection.
     * OPTIMIZED: Returns DTOs directly to avoid entity materialization overhead.
     * Only returns unsold listings.
     * 
     * @param categories list of category names to filter by (null for all)
     * @param minPrice minimum price threshold (null for no minimum)
     * @param maxPrice maximum price threshold (null for no maximum)
     * @param sortBy field to sort by ("price" or "date")
     * @param sortOrder sort direction ("asc" or "desc")
     * @param pageable pagination parameters
     * @return list of matching listing DTOs
     */
    @Query("SELECT new com.pond.server.dto.ListingDTO(" +
           "l.listingGU, l.userGU, l.title, l.description, l.picture1_url, l.picture2_url, " +
           "l.price, l.condition, l.category, l.createdAt, l.sold, l.soldTo) " +
           "FROM Listing l WHERE " +
           "l.sold = false AND " +
           "(COALESCE(:categories, NULL) IS NULL OR l.category IN :categories) AND " +
           "(:minPrice IS NULL OR l.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.price <= :maxPrice) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'price' AND :sortOrder = 'asc' THEN l.price END ASC NULLS LAST, " +
           "CASE WHEN :sortBy = 'price' AND :sortOrder = 'desc' THEN l.price END DESC NULLS LAST, " +
           "CASE WHEN :sortBy = 'date' AND :sortOrder = 'asc' THEN l.createdAt END ASC NULLS LAST, " +
           "CASE WHEN :sortBy = 'date' AND :sortOrder = 'desc' THEN l.createdAt END DESC NULLS LAST")
    List<ListingDTO> findFilteredDTOWithLimit(
        @Param("categories") List<String> categories,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("sortBy") String sortBy,
        @Param("sortOrder") String sortOrder,
        org.springframework.data.domain.Pageable pageable
    );
    
    /**
     * Convenience method for filtered searches with default pagination.
     * Delegates to {@link #findFilteredWithLimit} with limit of 500 results.
     * 
     * @param categories list of category names to filter by
     * @param minPrice minimum price threshold
     * @param maxPrice maximum price threshold
     * @param sortBy field to sort by
     * @param sortOrder sort direction
     * @param searchQuery search text
     * @return list of matching listings (max 500)
     */
    default List<Listing> findFiltered(List<String> categories, Double minPrice, Double maxPrice, 
                                       String sortBy, String sortOrder, String searchQuery) {
        return findFilteredWithLimit(categories, minPrice, maxPrice, sortBy, sortOrder, searchQuery,
                                    org.springframework.data.domain.PageRequest.of(0, 500));
    }
    
    /**
     * Finds listings from specific users (typically followed users) with filters.
     * OPTIMIZED: Prevents N+1 query problem by fetching all listings in one query.
     * 
     * @param userIds list of user UUIDs whose listings to fetch
     * @param categories list of category names to filter by (null for all)
     * @param minPrice minimum price threshold (null for no minimum)
     * @param maxPrice maximum price threshold (null for no maximum)
     * @param sortBy field to sort by ("price" or "date")
     * @param sortOrder sort direction ("asc" or "desc")
     * @param pageable pagination parameters
     * @return list of matching listings from specified users
     */
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
    
    /**
     * Finds listings from specific users using DTO projection.
     * OPTIMIZED: Returns DTOs directly for better performance.
     * Only returns unsold listings.
     * 
     * @param userIds list of user UUIDs whose listings to fetch
     * @param categories list of category names to filter by (null for all)
     * @param minPrice minimum price threshold (null for no minimum)
     * @param maxPrice maximum price threshold (null for no maximum)
     * @param sortBy field to sort by ("price" or "date")
     * @param sortOrder sort direction ("asc" or "desc")
     * @param pageable pagination parameters
     * @return list of matching listing DTOs from specified users
     */
    @Query("SELECT new com.pond.server.dto.ListingDTO(" +
           "l.listingGU, l.userGU, l.title, l.description, l.picture1_url, l.picture2_url, " +
           "l.price, l.condition, l.category, l.createdAt, l.sold, l.soldTo) " +
           "FROM Listing l WHERE " +
           "l.sold = false AND " +
           "l.userGU IN :userIds AND " +
           "(COALESCE(:categories, NULL) IS NULL OR l.category IN :categories) AND " +
           "(:minPrice IS NULL OR l.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.price <= :maxPrice) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'price' AND :sortOrder = 'asc' THEN l.price END ASC NULLS LAST, " +
           "CASE WHEN :sortBy = 'price' AND :sortOrder = 'desc' THEN l.price END DESC NULLS LAST, " +
           "CASE WHEN :sortBy = 'date' AND :sortOrder = 'asc' THEN l.createdAt END ASC NULLS LAST, " +
           "CASE WHEN :sortBy = 'date' AND :sortOrder = 'desc' THEN l.createdAt END DESC NULLS LAST")
    List<ListingDTO> findFollowingFilteredDTOWithLimit(
        @Param("userIds") List<UUID> userIds,
        @Param("categories") List<String> categories,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("sortBy") String sortBy,
        @Param("sortOrder") String sortOrder,
        org.springframework.data.domain.Pageable pageable
    );

    /**
     * Finds listings sold by a specific user to a specific buyer.
     * 
     * @param userGU UUID of the seller
     * @param soldTo UUID of the buyer
     * @return list of listings matching the criteria
     */
    List<Listing> findByUserGUAndSoldTo(UUID userGU, UUID soldTo);

    /**
     * Checks if a transaction occurred between two users.
     * Used to verify that users have completed a transaction before allowing reviews.
     * Returns true if either user sold to the other.
     * 
     * @param reviewerGu UUID of the reviewing user
     * @param revieweeGu UUID of the user being reviewed
     * @return true if a transaction occurred, false otherwise
     */
    @Query("""
    SELECT CASE WHEN COUNT(l) > 0 THEN TRUE ELSE FALSE END
    FROM Listing l
    WHERE l.soldTo IS NOT NULL
      AND (
            (l.userGU = :reviewerGu AND l.soldTo = :revieweeGu)
         OR (l.userGU = :revieweeGu AND l.soldTo = :reviewerGu)
      )
    """)
    boolean didTransactionOccur(
            @Param("reviewerGu") UUID reviewerGu,
            @Param("revieweeGu") UUID revieweeGu
    );


    /**
     * Convenience method for following feed with default pagination.
     * Delegates to {@link #findFollowingFilteredWithLimit} with limit of 500 results.
     * 
     * @param userIds list of user UUIDs to fetch listings from
     * @param categories list of category names to filter by
     * @param minPrice minimum price threshold
     * @param maxPrice maximum price threshold
     * @param sortBy field to sort by
     * @param sortOrder sort direction
     * @return list of matching listings (max 500)
     */
    default List<Listing> findFollowingFiltered(List<UUID> userIds, List<String> categories, 
                                                 Double minPrice, Double maxPrice, 
                                                 String sortBy, String sortOrder) {
        return findFollowingFilteredWithLimit(userIds, categories, minPrice, maxPrice, sortBy, sortOrder,
                                             org.springframework.data.domain.PageRequest.of(0, 500));
    }
}
