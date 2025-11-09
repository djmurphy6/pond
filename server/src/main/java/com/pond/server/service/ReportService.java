package com.pond.server.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pond.server.dto.CreateReportRequest;
import com.pond.server.dto.ReportDTO;
import com.pond.server.dto.UpdateReportRequest;
import com.pond.server.enums.ReportReason;
import com.pond.server.enums.ReportStatus;
import com.pond.server.model.Report;
import com.pond.server.repository.ListingRepository;
import com.pond.server.repository.ReportRepository;
import com.pond.server.repository.UserRepository;

@Service
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public ReportService(ReportRepository reportRepository, UserRepository userRepository, ListingRepository listingRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }
    
    // Create a new report
    public ReportDTO createReport(UUID userGU, CreateReportRequest request) {
        // Check if user already reported this listing
        if (reportRepository.findByUserGUAndListingGU(userGU, 
            UUID.fromString(request.getListingGU())).isPresent()) {  
            throw new RuntimeException("You have already reported this listing");
        }
        
        Report report = new Report(
            userGU,
            UUID.fromString(request.getListingGU()), 
            ReportReason.valueOf(request.getReason()),  
            request.getMessage()  
        );
        
        return mapToDTO(reportRepository.save(report));
    }
    
    // Get all reports with pagination (admin only)
    public Page<ReportDTO> getAllReports(Pageable pageable) {
        return reportRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(this::mapToDTO);
    }
    
    // Get reports by status (admin only)
    public Page<ReportDTO> getReportsByStatus(ReportStatus status, Pageable pageable) {
        return reportRepository.findByStatus(status, pageable)
            .map(this::mapToDTO);
    }
    
    // Update report status (admin only)
    public ReportDTO updateReportStatus(UUID reportGU, UUID adminGU, 
                                        UpdateReportRequest request) {
        Report report = reportRepository.findById(reportGU)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        
        report.setStatus(ReportStatus.valueOf(request.getStatus()));
        report.setAdminNotes(request.getAdminNotes());
        report.setReviewedByAdminGU(adminGU);
        report.setReviewedAt(LocalDateTime.now());
        
        return mapToDTO(reportRepository.save(report));
    }
    
    // Get count of pending reports (for notification badge)
    public long getPendingReportCount() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }
    
    // Get reports filed by a user (outgoing reports)
    public Page<ReportDTO> getUserOutgoingReports(UUID userGU, Pageable pageable) {
        return reportRepository.findByUserGUOrderByCreatedAtDesc(userGU, pageable)
            .map(this::mapToDTO);
    }
    
    // Get reports made against a user's listings (incoming reports)
    public Page<ReportDTO> getUserIncomingReports(UUID userGU, Pageable pageable) {
        // First get all listings owned by the user
        var userListings = listingRepository.findByUserGU(userGU);
        var listingGUs = userListings.stream()
            .map(listing -> listing.getListingGU())
            .toList();
        
        // If user has no listings, return empty page
        if (listingGUs.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Get all reports for those listings
        return reportRepository.findByListingGUInOrderByCreatedAtDesc(listingGUs, pageable)
            .map(this::mapToDTO);
    }
    
    private ReportDTO mapToDTO(Report report) {
        // Fetch user and listing details
        var user = userRepository.findById(report.getUserGU()).orElse(null);
        var listing = listingRepository.findById(report.getListingGU()).orElse(null);
        
        return new ReportDTO(
            report.getReportGU().toString(),
            report.getUserGU().toString(),
            user != null ? user.getUsername() : "Unknown",
            report.getListingGU().toString(),
            listing != null ? listing.getTitle() : "Deleted Listing",
            report.getReason(),
            report.getMessage(),
            report.getStatus(),
            report.getCreatedAt(),
            report.getReviewedByAdminGU() != null ? 
                report.getReviewedByAdminGU().toString() : null,
            report.getReviewedAt(),
            report.getAdminNotes()
        );
    }
}