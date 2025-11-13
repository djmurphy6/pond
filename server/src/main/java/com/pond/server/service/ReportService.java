package com.pond.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.pond.server.dto.CreateReportRequest;
import com.pond.server.dto.ReportDTO;
import com.pond.server.dto.UpdateReportRequest;
import com.pond.server.enums.ReportReason;
import com.pond.server.enums.ReportStatus;
import com.pond.server.model.Listing;
import com.pond.server.model.Report;
import com.pond.server.model.ResolvedReport;
import com.pond.server.model.User;
import com.pond.server.repository.ListingRepository;
import com.pond.server.repository.ReportRepository;
import com.pond.server.repository.ResolvedReportRepository;
import com.pond.server.repository.UserRepository;

@Service
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final ResolvedReportRepository resolvedReportRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    public ReportService(ReportRepository reportRepository, 
                         ResolvedReportRepository resolvedReportRepository,
                         UserRepository userRepository, 
                         ListingRepository listingRepository) {
        this.reportRepository = reportRepository;
        this.resolvedReportRepository = resolvedReportRepository;
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
    // OPTIMIZED: Batch fetches users and listings to prevent N+1 queries
    public Page<ReportDTO> getAllReports(Pageable pageable) {
        Page<Report> reports = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapToDTOWithBatchFetch(reports);
    }
    
    // Get reports by status (admin only)
    // OPTIMIZED: Batch fetches users and listings to prevent N+1 queries
    public Page<ReportDTO> getReportsByStatus(ReportStatus status, Pageable pageable) {
        Page<Report> reports = reportRepository.findByStatus(status, pageable);
        return mapToDTOWithBatchFetch(reports);
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
    // OPTIMIZED: Batch fetches users and listings to prevent N+1 queries
    public Page<ReportDTO> getUserOutgoingReports(UUID userGU, Pageable pageable) {
        Page<Report> reports = reportRepository.findByUserGUOrderByCreatedAtDesc(userGU, pageable);
        return mapToDTOWithBatchFetch(reports);
    }
    
    // Get reports made against a user's listings (incoming reports)
    // OPTIMIZED: Uses single JOIN query instead of fetching all listings first
    // OPTIMIZED: Batch fetches users and listings to prevent N+1 queries
    public Page<ReportDTO> getUserIncomingReports(UUID userGU, Pageable pageable) {
        // Single query with JOIN - filters reports by listing ownership in database
        Page<Report> reports = reportRepository.findReportsByListingOwner(userGU, pageable);
        return mapToDTOWithBatchFetch(reports);
    }
    
    // Get all resolved (archived) reports - admin only
    // OPTIMIZED: Batch fetches users and listings to prevent N+1 queries
    public Page<ReportDTO> getAllResolvedReports(Pageable pageable) {
        Page<ResolvedReport> resolvedReports = resolvedReportRepository.findAll(pageable);
        return mapResolvedToDTOWithBatchFetch(resolvedReports);
    }
    
    // Get count of reports (for admin dashboard statistics)
    public long getTotalReportCount() {
        return reportRepository.count();
    }
    
    public long getTotalResolvedReportCount() {
        return resolvedReportRepository.count();
    }
    
    /**
     * OPTIMIZED: Batch fetch helper to prevent N+1 queries
     * Fetches all related users and listings in 2 queries instead of N*2 queries
     */
    private Page<ReportDTO> mapToDTOWithBatchFetch(Page<Report> reports) {
        if (reports.isEmpty()) {
            return reports.map(this::mapToDTO);
        }
        
        // Collect all unique user and listing IDs from the reports
        List<UUID> userGUs = reports.stream()
            .map(Report::getUserGU)
            .distinct()
            .collect(Collectors.toList());
        
        List<UUID> listingGUs = reports.stream()
            .map(Report::getListingGU)
            .distinct()
            .collect(Collectors.toList());
        
        // Batch fetch all users and listings (2 queries total instead of N*2)
        Map<UUID, User> userMap = StreamSupport.stream(userRepository.findAllById(userGUs).spliterator(), false)
            .collect(Collectors.toMap(User::getUserGU, user -> user));
        
        Map<UUID, Listing> listingMap = StreamSupport.stream(listingRepository.findAllById(listingGUs).spliterator(), false)
            .collect(Collectors.toMap(Listing::getListingGU, listing -> listing));
        
        // Map using cached data - no more queries!
        return reports.map(report -> mapToDTOWithCache(report, userMap, listingMap));
    }
    
    /**
     * OPTIMIZED: Batch fetch helper for resolved reports
     */
    private Page<ReportDTO> mapResolvedToDTOWithBatchFetch(Page<ResolvedReport> reports) {
        if (reports.isEmpty()) {
            return reports.map(this::mapResolvedToDTO);
        }
        
        // Collect all unique user and listing IDs from the reports
        List<UUID> userGUs = reports.stream()
            .map(ResolvedReport::getUserGU)
            .distinct()
            .collect(Collectors.toList());
        
        List<UUID> listingGUs = reports.stream()
            .map(ResolvedReport::getListingGU)
            .distinct()
            .collect(Collectors.toList());
        
        // Batch fetch all users and listings
        Map<UUID, User> userMap = StreamSupport.stream(userRepository.findAllById(userGUs).spliterator(), false)
            .collect(Collectors.toMap(User::getUserGU, user -> user));
        
        Map<UUID, Listing> listingMap = StreamSupport.stream(listingRepository.findAllById(listingGUs).spliterator(), false)
            .collect(Collectors.toMap(Listing::getListingGU, listing -> listing));
        
        // Map using cached data
        return reports.map(report -> mapResolvedToDTOWithCache(report, userMap, listingMap));
    }
    
    /**
     * Map Report to DTO using pre-fetched cache (O(1) lookup instead of N queries)
     */
    private ReportDTO mapToDTOWithCache(Report report, Map<UUID, User> userMap, Map<UUID, Listing> listingMap) {
        User user = userMap.get(report.getUserGU());
        Listing listing = listingMap.get(report.getListingGU());
        
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
    
    /**
     * Map ResolvedReport to DTO using pre-fetched cache
     */
    private ReportDTO mapResolvedToDTOWithCache(ResolvedReport report, Map<UUID, User> userMap, Map<UUID, Listing> listingMap) {
        User user = userMap.get(report.getUserGU());
        Listing listing = listingMap.get(report.getListingGU());
        
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
    
    private ReportDTO mapResolvedToDTO(ResolvedReport report) {
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