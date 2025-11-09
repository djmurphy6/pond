package com.pond.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
    
    // Check if a user already reported a listing (prevent duplicate reports)
    Optional<Report> findByUserGUAndListingGU(UUID userGU, UUID listingGU);
    
    // Count pending reports (for admin notification badge)
    long countByStatus(ReportStatus status);
    
    // Get reports ordered by creation date
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);
}