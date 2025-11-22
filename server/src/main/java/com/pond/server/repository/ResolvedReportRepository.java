package com.pond.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pond.server.model.ResolvedReport;

@Repository
public interface ResolvedReportRepository extends JpaRepository<ResolvedReport, UUID> {
    // Find all resolved reports for a specific listing
    List<ResolvedReport> findByListingGU(UUID listingGU);
    
    // Delete all resolved reports for a listing
    void deleteByListingGU(UUID listingGU);
}

