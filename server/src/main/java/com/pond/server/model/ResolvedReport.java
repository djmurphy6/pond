package com.pond.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pond.server.enums.ReportReason;
import com.pond.server.enums.ReportStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an archived report that has been resolved.
 * 
 * <p>When a {@link Report} is reviewed and resolved (approved or rejected),
 * it is moved from the active reports table to this archive table. This
 * keeps the active reports queue clean while maintaining a complete history
 * of all reports for auditing purposes.</p>
 * 
 * <p>The structure mirrors the {@link Report} entity with an additional
 * {@link #archivedAt} timestamp to track when the report was archived.</p>
 * 
 * @author Pond Team
 * @see Report
 */
@Entity
@Table(name = "resolved_reports")
@Getter
@Setter
@NoArgsConstructor
public class ResolvedReport {
    
    /**
     * Unique identifier for the report (UUID).
     * Uses the same ID as the original Report entity.
     */
    @Id
    @Column(name = "report_gu", updatable = false, nullable = false)
    private UUID reportGU;

    /**
     * UUID of the user who filed the original report.
     */
    @Column(name = "user_gu", nullable = false)
    private UUID userGU;

    /**
     * UUID of the listing that was reported.
     */
    @Column(name = "listing_gu", nullable = false)
    private UUID listingGU;

    /**
     * Categorized reason for the report.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;

    /**
     * Optional detailed message explaining the report.
     * Maximum length of 1000 characters.
     */
    @Column(name = "message", length = 1000)
    private String message;

    /**
     * Final status of the report (APPROVED or REJECTED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    /**
     * Timestamp when the original report was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * UUID of the administrator who reviewed the report.
     */
    @Column(name = "reviewed_by_admin_gu")
    private UUID reviewedByAdminGU;

    /**
     * Timestamp when the report was reviewed.
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * Notes added by the administrator during review.
     * Maximum length of 1000 characters.
     */
    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;
    
    /**
     * Timestamp when the report was moved to the resolved_reports table.
     * Set automatically in the constructor.
     */
    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;

    /**
     * Constructs a ResolvedReport from an existing Report.
     * Copies all fields from the report and sets archivedAt to current time.
     * 
     * @param report the original report to archive
     */
    public ResolvedReport(Report report) {
        this.reportGU = report.getReportGU();
        this.userGU = report.getUserGU();
        this.listingGU = report.getListingGU();
        this.reason = report.getReason();
        this.message = report.getMessage();
        this.status = report.getStatus();
        this.createdAt = report.getCreatedAt();
        this.reviewedByAdminGU = report.getReviewedByAdminGU();
        this.reviewedAt = report.getReviewedAt();
        this.adminNotes = report.getAdminNotes();
        this.archivedAt = LocalDateTime.now();
    }
}

