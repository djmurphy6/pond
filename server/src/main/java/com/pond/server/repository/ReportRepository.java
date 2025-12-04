package com.pond.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pond.server.enums.ReportStatus;
import com.pond.server.model.Report;

/**
 * Repository interface for {@link Report} entity database operations.
 * 
 * <p>Manages user reports against marketplace listings, supporting admin review
 * workflows with status filtering and pagination. Includes optimized queries to
 * fetch reports for listing owners and prevent duplicate reporting.</p>
 * 
 * @author Pond Team
 * @see Report
 * @see ResolvedReport
 * @see com.pond.server.enums.ReportStatus
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    
    /**
     * Gets all reports filed against a specific listing.
     * 
     * @param listingGU UUID of the listing
     * @return list of reports for the listing
     */
    List<Report> findByListingGU(UUID listingGU);
    
    /**
     * Gets reports filtered by status with pagination.
     * Used in admin dashboard to view pending/approved/rejected reports.
     * 
     * @param status the report status to filter by
     * @param pageable pagination parameters
     * @return page of reports matching the status
     */
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
    
    /**
     * Gets all reports filed by a specific user.
     * 
     * @param userGU UUID of the reporting user
     * @return list of reports filed by the user
     */
    List<Report> findByUserGU(UUID userGU);
    
    /**
     * Gets reports filed by a user, ordered by creation date (newest first).
     * 
     * @param userGU UUID of the reporting user
     * @param pageable pagination parameters
     * @return page of reports ordered by creation date descending
     */
    Page<Report> findByUserGUOrderByCreatedAtDesc(UUID userGU, Pageable pageable);
    
    /**
     * Gets reports for specific listings, ordered by creation date.
     * 
     * @param listingGUs list of listing UUIDs
     * @param pageable pagination parameters
     * @return page of reports for the specified listings
     */
    Page<Report> findByListingGUInOrderByCreatedAtDesc(List<UUID> listingGUs, Pageable pageable);
    
    /**
     * Gets reports against a user's listings using JOIN query.
     * OPTIMIZED: Avoids fetching all user listings first by using JOIN.
     * Used to show listing owners what reports their listings have received.
     * 
     * @param userGU UUID of the listing owner
     * @param pageable pagination parameters
     * @return page of reports against the user's listings
     */
    @Query("SELECT r FROM Report r " +
           "JOIN Listing l ON r.listingGU = l.listingGU " +
           "WHERE l.userGU = :userGU " +
           "ORDER BY r.createdAt DESC")
    Page<Report> findReportsByListingOwner(@Param("userGU") UUID userGU, Pageable pageable);
    
    /**
     * Checks if a user has already reported a specific listing.
     * Used to prevent duplicate reports from the same user.
     * 
     * @param userGU UUID of the reporting user
     * @param listingGU UUID of the listing
     * @return an Optional containing the report if it exists
     */
    Optional<Report> findByUserGUAndListingGU(UUID userGU, UUID listingGU);
    
    /**
     * Counts reports with a specific status.
     * Used for admin notification badge showing pending report count.
     * 
     * @param status the report status to count
     * @return count of reports with the specified status
     */
    long countByStatus(ReportStatus status);
    
    /**
     * Gets all reports ordered by creation date (newest first).
     * 
     * @param pageable pagination parameters
     * @return page of all reports ordered by creation date descending
     */
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
}