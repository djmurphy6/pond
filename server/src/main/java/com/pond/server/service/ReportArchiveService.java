package com.pond.server.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pond.server.enums.ReportStatus;
import com.pond.server.model.Report;
import com.pond.server.model.ResolvedReport;
import com.pond.server.repository.ReportRepository;
import com.pond.server.repository.ResolvedReportRepository;

/**
 * Service class for automatically archiving resolved reports.
 * Runs scheduled tasks to move resolved reports older than 24 hours to an archive table.
 * Also runs on application startup to catch up on any missed archiving.
 */
@Service
public class ReportArchiveService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportArchiveService.class);
    
    private final ReportRepository reportRepository;
    private final ResolvedReportRepository resolvedReportRepository;

    /**
     * Constructs a new ReportArchiveService with required dependencies.
     *
     * @param reportRepository the repository for active report data access
     * @param resolvedReportRepository the repository for archived report data access
     */
    public ReportArchiveService(ReportRepository reportRepository, 
                                ResolvedReportRepository resolvedReportRepository) {
        this.reportRepository = reportRepository;
        this.resolvedReportRepository = resolvedReportRepository;
    }
    
    /**
     * Runs on application startup to catch up on reports that should have been archived.
     * Archives any resolved reports that have been resolved for 24+ hours.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onStartup() {
        logger.info("Application started - running initial report archive check");
        performArchiving();
    }
    
    /**
     * Scheduled task that runs every hour to archive old resolved reports.
     * Archives reports that have been in resolved status for 24+ hours.
     * Cron expression: "0 0 * * * *" = every hour at minute 0
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void archiveOldResolvedReports() {
        logger.info("Starting scheduled task: archiveOldResolvedReports");
        performArchiving();
    }
    
    /**
     * Core archiving logic that moves resolved reports to the archive table.
     * Finds all resolved reports older than 24 hours, copies them to resolved_reports table,
     * and deletes them from the reports table.
     * Must be called from a @Transactional method for proper transaction management.
     */
    private void performArchiving() {
        try {
            LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
            logger.info("Looking for reports resolved before: {}", twentyFourHoursAgo);
            
            // Find all resolved reports where reviewedAt is more than 24 hours ago
            List<Report> reportsToArchive = reportRepository.findAll().stream()
                .filter(report -> report.getStatus() == ReportStatus.RESOLVED)
                .filter(report -> report.getReviewedAt() != null)
                .filter(report -> report.getReviewedAt().isBefore(twentyFourHoursAgo))
                .toList();
            
            if (reportsToArchive.isEmpty()) {
                logger.info("No reports found to archive");
                return;
            }
            
            logger.info("Found {} reports to archive", reportsToArchive.size());
            
            // Archive each report
            int successCount = 0;
            for (Report report : reportsToArchive) {
                try {
                    logger.info("Archiving report {} - reviewedAt: {}", 
                               report.getReportGU(), report.getReviewedAt());
                    
                    // Create resolved report entry
                    ResolvedReport resolvedReport = new ResolvedReport(report);
                    resolvedReportRepository.save(resolvedReport);
                    resolvedReportRepository.flush(); // Force immediate write
                    
                    logger.info("Saved to resolved_reports table: {}", report.getReportGU());
                    
                    // Delete from reports table
                    reportRepository.delete(report);
                    reportRepository.flush(); // Force immediate delete
                    
                    logger.info("Deleted from reports table: {}", report.getReportGU());
                    successCount++;
                    
                } catch (Exception e) {
                    logger.error("Failed to archive report {}: {}", 
                                report.getReportGU(), e.getMessage(), e);
                    throw e; // Re-throw to roll back transaction for this report
                }
            }
            
            logger.info("Successfully archived {} reports", successCount);
            
        } catch (Exception e) {
            logger.error("Error during report archiving: {}", e.getMessage(), e);
            throw e; // Re-throw to ensure transaction rollback
        }
    }
    
    /**
     * Manually archives a specific report immediately, bypassing the 24-hour wait.
     * Can be called by admins if needed for immediate archiving.
     *
     * @param report the report to archive
     */
    @Transactional
    public void archiveReportManually(Report report) {
        logger.info("Manually archiving report {}", report.getReportGU());
        
        ResolvedReport resolvedReport = new ResolvedReport(report);
        resolvedReportRepository.save(resolvedReport);
        reportRepository.delete(report);
        
        logger.info("Successfully archived report {}", report.getReportGU());
    }
}

