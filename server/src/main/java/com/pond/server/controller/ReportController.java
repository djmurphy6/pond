package com.pond.server.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pond.server.dto.CreateReportRequest;
import com.pond.server.dto.ReportDTO;
import com.pond.server.dto.UpdateReportRequest;
import com.pond.server.model.User;
import com.pond.server.service.ReportService;

/**
 * REST controller for report management operations.
 * Handles report creation, status updates, retrieval, and statistics (admin features).
 */
@RestController
@RequestMapping("/reports")
public class ReportController {
    
    private final ReportService reportService;

    /**
     * Constructs a new ReportController with required dependencies.
     *
     * @param reportService the service for report operations
     */
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    
    /**
     * Creates a new report for a listing.
     *
     * @param user the authenticated user filing the report
     * @param request the create report request with listing ID, reason, and message
     * @return ResponseEntity with the created report
     */
    @PostMapping
    public ResponseEntity<ReportDTO> createReport(
            @AuthenticationPrincipal User user,
            @RequestBody CreateReportRequest request) {
        return ResponseEntity.ok(reportService.createReport(user.getUserGU(), request));
    }
    
    /**
     * Retrieves all reports with pagination (admin only).
     *
     * @param user the authenticated admin user
     * @param page the page number (default 0)
     * @param size the page size (default 20)
     * @return ResponseEntity with paginated reports or 403 if not admin
     */
    @GetMapping("/admin")
    public ResponseEntity<Page<ReportDTO>> getAllReports(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (!user.getAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(
            reportService.getAllReports(PageRequest.of(page, size)));
    }
    
    /**
     * Retrieves the count of pending reports (admin only).
     * Used for admin notification badges.
     *
     * @param user the authenticated admin user
     * @return ResponseEntity with pending report count or 403 if not admin
     */
    @GetMapping("/admin/pending-count")
    public ResponseEntity<Long> getPendingCount(@AuthenticationPrincipal User user) {
        if (!user.getAdmin()) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(reportService.getPendingReportCount());
    }
    
    /**
     * Updates a report's status and admin notes (admin only).
     *
     * @param user the authenticated admin user
     * @param reportGU the UUID string of the report
     * @param request the update request with new status and admin notes
     * @return ResponseEntity with updated report or 403 if not admin
     */
    @PutMapping("/admin/{reportGU}")
    public ResponseEntity<ReportDTO> updateReport(
            @AuthenticationPrincipal User user,
            @PathVariable String reportGU,
            @RequestBody UpdateReportRequest request) {
        
        if (!user.getAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(
            reportService.updateReportStatus(
                UUID.fromString(reportGU), 
                user.getUserGU(), 
                request));
    }
    
    /**
     * Retrieves reports filed by the authenticated user.
     *
     * @param user the authenticated user
     * @param page the page number (default 0)
     * @param size the page size (default 20)
     * @return ResponseEntity with paginated outgoing reports
     */
    @GetMapping("/my-reports/outgoing")
    public ResponseEntity<Page<ReportDTO>> getMyOutgoingReports(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(
            reportService.getUserOutgoingReports(
                user.getUserGU(), 
                PageRequest.of(page, size)));
    }
    
    /**
     * Retrieves reports made against the authenticated user's listings.
     *
     * @param user the authenticated user
     * @param page the page number (default 0)
     * @param size the page size (default 20)
     * @return ResponseEntity with paginated incoming reports
     */
    @GetMapping("/my-reports/incoming")
    public ResponseEntity<Page<ReportDTO>> getMyIncomingReports(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(
            reportService.getUserIncomingReports(
                user.getUserGU(), 
                PageRequest.of(page, size)));
    }
    
    /**
     * Retrieves all resolved (archived) reports with pagination (admin only).
     *
     * @param user the authenticated admin user
     * @param page the page number (default 0)
     * @param size the page size (default 20)
     * @return ResponseEntity with paginated resolved reports or 403 if not admin
     */
    @GetMapping("/admin/resolved")
    public ResponseEntity<Page<ReportDTO>> getAllResolvedReports(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (!user.getAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(
            reportService.getAllResolvedReports(PageRequest.of(page, size)));
    }
    
    /**
     * Retrieves report statistics (admin only).
     * Includes counts for active, resolved, and pending reports.
     *
     * @param user the authenticated admin user
     * @return ResponseEntity with report statistics or 403 if not admin
     */
    @GetMapping("/admin/statistics")
    public ResponseEntity<ReportStatistics> getReportStatistics(
            @AuthenticationPrincipal User user) {
        
        if (!user.getAdmin()) {
            return ResponseEntity.status(403).build();
        }
        
        long activeReports = reportService.getTotalReportCount();
        long resolvedReports = reportService.getTotalResolvedReportCount();
        long pendingReports = reportService.getPendingReportCount();
        
        return ResponseEntity.ok(new ReportStatistics(activeReports, resolvedReports, pendingReports));
    }
    
    /**
     * Record representing report statistics.
     *
     * @param activeReports the count of active reports
     * @param resolvedReports the count of resolved reports
     * @param pendingReports the count of pending reports
     */
    public record ReportStatistics(long activeReports, long resolvedReports, long pendingReports) {}
}