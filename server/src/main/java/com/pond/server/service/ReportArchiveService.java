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

@Service
public class ReportArchiveService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportArchiveService.class);
    
    private final ReportRepository reportRepository;
    private final ResolvedReportRepository resolvedReportRepository;

    public ReportArchiveService(ReportRepository reportRepository, 
                                ResolvedReportRepository resolvedReportRepository) {
        this.reportRepository = reportRepository;
        this.resolvedReportRepository = resolvedReportRepository;
    }
    
    /*
     * Runs when the application starts to catch up on any reports
     * that should have been archived while the server was down.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onStartup() {
        logger.info("Application started - running initial report archive check");
        performArchiving();
    }
    
    /*
     * Runs every hour to check for resolved reports that have been 
     * in resolved status for 24+ hours and archives them.
     * Cron expression: "0 0 * * * *" = every hour at minute 0
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void archiveOldResolvedReports() {
        logger.info("Starting scheduled task: archiveOldResolvedReports");
        performArchiving();
    }
    
    /**
     * Core archiving logic - must be called from a @Transactional method
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
     * Manual method to archive a specific report immediately
     * Can be called by admins if needed
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

