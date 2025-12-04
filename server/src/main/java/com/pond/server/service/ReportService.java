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
import org.springframework.transaction.annotation.Transactional;

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

/**
 * Service class for managing listing reports.
 * Handles report creation, status updates, retrieval with optimization for N+1 query prevention.
 */
@Service
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final ResolvedReportRepository resolvedReportRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;

    /**
     * Constructs a new ReportService with required dependencies.
     *
     * @param reportRepository the repository for report data access
     * @param resolvedReportRepository the repository for resolved report data access
     * @param userRepository the repository for user data access
     * @param listingRepository the repository for listing data access
     */
    public ReportService(ReportRepository reportRepository, 
                         ResolvedReportRepository resolvedReportRepository,
                         UserRepository userRepository, 
                         ListingRepository listingRepository) {
        this.reportRepository = reportRepository;
        this.resolvedReportRepository = resolvedReportRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
    }
    
    /**
     * Creates a new report for a listing.
     * Prevents duplicate reports from the same user for the same listing.
     *
     * @param userGU the UUID of the user filing the report
     * @param request the create report request containing listing ID, reason, and message
     * @return the created ReportDTO
     * @throws RuntimeException if user has already reported this listing
     */
    @Transactional
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
    
    /**
     * Retrieves all reports with pagination (admin only).
     * OPTIMIZED: Batch fetches users and listings to prevent N+1 queries.
     *
     * @param pageable pagination parameters
     * @return a page of reports ordered by creation date (descending)
     */
    @Transactional(readOnly = true)
    public Page<ReportDTO> getAllReports(Pageable pageable) {
        Page<Report> reports = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapToDTOWithBatchFetch(reports);
    }
    
    /**
     * Retrieves reports filtered by status with pagination (admin only).
     * OPTIMIZED: Batch fetches users and listings to prevent N+1 queries.
     *
     * @param status the report status to filter by
     * @param pageable pagination parameters
     * @return a page of reports with the specified status
     */
    @Transactional(readOnly = true)
    public Page<ReportDTO> getReportsByStatus(ReportStatus status, Pageable pageable) {
        Page<Report> reports = reportRepository.findByStatus(status, pageable);
        return mapToDTOWithBatchFetch(reports);
    }
    
    /**
     * Updates the status of a report (admin only).
     * Records the admin who reviewed it and the review timestamp.
     *
     * @param reportGU the UUID of the report to update
     * @param adminGU the UUID of the admin reviewing the report
     * @param request the update request containing new status and admin notes
     * @return the updated ReportDTO
     * @throws RuntimeException if report not found
     */
    @Transactional
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
    
    /**
     * Gets the count of pending reports.
     * Used for admin notification badges.
     *
     * @return the number of reports with PENDING status
     */
    @Transactional(readOnly = true)
    public long getPendingReportCount() {
        return reportRepository.countByStatus(ReportStatus.PENDING);
    }
    
    /**
     * Retrieves reports filed by a specific user (outgoing reports).
     * OPTIMIZED: Batch fetches users and listings to prevent N+1 queries.
     *
     * @param userGU the UUID of the user who filed the reports
     * @param pageable pagination parameters
     * @return a page of reports filed by the user
     */
    @Transactional(readOnly = true)
    public Page<ReportDTO> getUserOutgoingReports(UUID userGU, Pageable pageable) {
        Page<Report> reports = reportRepository.findByUserGUOrderByCreatedAtDesc(userGU, pageable);
        return mapToDTOWithBatchFetch(reports);
    }
    
    /**
     * Retrieves reports made against a user's listings (incoming reports).
     * OPTIMIZED: Uses single JOIN query instead of fetching all listings first.
     * OPTIMIZED: Batch fetches users and listings to prevent N+1 queries.
     *
     * @param userGU the UUID of the user whose listings were reported
     * @param pageable pagination parameters
     * @return a page of reports against the user's listings
     */
    @Transactional(readOnly = true)
    public Page<ReportDTO> getUserIncomingReports(UUID userGU, Pageable pageable) {
        // Single query with JOIN - filters reports by listing ownership in database
        Page<Report> reports = reportRepository.findReportsByListingOwner(userGU, pageable);
        return mapToDTOWithBatchFetch(reports);
    }
    
    /**
     * Retrieves all resolved (archived) reports with pagination (admin only).
     * OPTIMIZED: Batch fetches users and listings to prevent N+1 queries.
     *
     * @param pageable pagination parameters
     * @return a page of resolved reports
     */
    @Transactional(readOnly = true)
    public Page<ReportDTO> getAllResolvedReports(Pageable pageable) {
        Page<ResolvedReport> resolvedReports = resolvedReportRepository.findAll(pageable);
        return mapResolvedToDTOWithBatchFetch(resolvedReports);
    }
    
    /**
     * Gets the total count of active reports.
     * Used for admin dashboard statistics.
     *
     * @return the total number of active reports
     */
    @Transactional(readOnly = true)
    public long getTotalReportCount() {
        return reportRepository.count();
    }
    
    /**
     * Gets the total count of resolved reports.
     * Used for admin dashboard statistics.
     *
     * @return the total number of resolved reports
     */
    @Transactional(readOnly = true)
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
     * Maps a Report to ReportDTO using pre-fetched cache.
     * Provides O(1) lookup instead of N database queries.
     *
     * @param report the report entity to map
     * @param userMap cached map of user UUIDs to User entities
     * @param listingMap cached map of listing UUIDs to Listing entities
     * @return the ReportDTO representation
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
     * Maps a ResolvedReport to ReportDTO using pre-fetched cache.
     * Provides O(1) lookup instead of N database queries.
     *
     * @param report the resolved report entity to map
     * @param userMap cached map of user UUIDs to User entities
     * @param listingMap cached map of listing UUIDs to Listing entities
     * @return the ReportDTO representation
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
    
    /**
     * Maps a Report entity to ReportDTO with individual database queries.
     * Used when not batch fetching (single report operations).
     *
     * @param report the report entity to map
     * @return the ReportDTO representation
     */
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
    
    /**
     * Maps a ResolvedReport entity to ReportDTO with individual database queries.
     * Used when not batch fetching (single report operations).
     *
     * @param report the resolved report entity to map
     * @return the ReportDTO representation
     */
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