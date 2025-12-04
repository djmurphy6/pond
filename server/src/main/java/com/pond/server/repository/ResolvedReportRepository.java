package com.pond.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pond.server.model.ResolvedReport;

/**
 * Repository interface for {@link ResolvedReport} entity database operations.
 * 
 * <p>Manages archived reports that have been reviewed and resolved by administrators.
 * This table serves as a historical record of all reports while keeping the active
 * reports queue clean and performant.</p>
 * 
 * @author Pond Team
 * @see ResolvedReport
 * @see Report
 */
@Repository
public interface ResolvedReportRepository extends JpaRepository<ResolvedReport, UUID> {
    
    /**
     * Finds all resolved reports for a specific listing.
     * Used to view report history for a listing.
     * 
     * @param listingGU UUID of the listing
     * @return list of resolved reports for the listing
     */
    List<ResolvedReport> findByListingGU(UUID listingGU);
    
    /**
     * Deletes all resolved reports for a specific listing.
     * Called when a listing is permanently deleted to maintain referential integrity.
     * 
     * @param listingGU UUID of the listing
     */
    void deleteByListingGU(UUID listingGU);
}

