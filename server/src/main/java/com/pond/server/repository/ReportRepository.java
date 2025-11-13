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

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    
    // Get all reports for a specific listing
    List<Report> findByListingGU(UUID listingGU);
    
    // Get reports by status (for admin dashboard filtering)
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
    
    // Get all reports filed by a user
    List<Report> findByUserGU(UUID userGU);
    
    // Get all reports filed by a user, ordered by creation date
    Page<Report> findByUserGUOrderByCreatedAtDesc(UUID userGU, Pageable pageable);
    
    // Get all reports for listings that belong to a specific user (reports against their listings)
    Page<Report> findByListingGUInOrderByCreatedAtDesc(List<UUID> listingGUs, Pageable pageable);
    
    // OPTIMIZED: Get reports for user's listings using JOIN (avoids fetching all listings first)
    @Query("SELECT r FROM Report r " +
           "JOIN Listing l ON r.listingGU = l.listingGU " +
           "WHERE l.userGU = :userGU " +
           "ORDER BY r.createdAt DESC")
    Page<Report> findReportsByListingOwner(@Param("userGU") UUID userGU, Pageable pageable);
    
    // Check if a user already reported a listing (prevent duplicate reports)
    Optional<Report> findByUserGUAndListingGU(UUID userGU, UUID listingGU);
    
    // Count pending reports (for admin notification badge)
    long countByStatus(ReportStatus status);
    
    // Get reports ordered by creation date
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
}