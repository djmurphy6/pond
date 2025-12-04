package com.pond.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pond.server.enums.ReportReason;
import com.pond.server.enums.ReportStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a user report against a marketplace listing.
 * 
 * <p>Users can report listings that violate community guidelines or contain
 * inappropriate content. Reports are reviewed by administrators who can
 * approve or reject them. The status progresses from PENDING to either
 * APPROVED or REJECTED.</p>
 * 
 * <p>Once resolved, reports are archived to the {@link ResolvedReport} table
 * to maintain a clean active reports queue while preserving history.</p>
 * 
 * @author Pond Team
 * @see ResolvedReport
 * @see com.pond.server.enums.ReportStatus
 * @see com.pond.server.enums.ReportReason
 */
@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {
    
    /**
     * Unique identifier for the report (UUID).
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_gu", updatable = false, nullable = false)
    private UUID reportGU;

    /**
     * UUID of the user who filed the report.
     */
    @Column(name = "user_gu", nullable = false)
    private UUID userGU;

    /**
     * UUID of the listing being reported.
     */
    @Column(name = "listing_gu", nullable = false)
    private UUID listingGU;

    /**
     * Categorized reason for the report.
     * Enum values like SPAM, INAPPROPRIATE_CONTENT, SCAM, etc.
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
     * Current status of the report (PENDING, APPROVED, REJECTED).
     * Defaults to PENDING when created.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    /**
     * Timestamp when the report was created.
     * Set automatically in the constructor.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * UUID of the administrator who reviewed the report.
     * Null until an admin reviews it.
     */
    @Column(name = "reviewed_by_admin_gu")
    private UUID reviewedByAdminGU;

    /**
     * Timestamp when the report was reviewed by an admin.
     * Null until reviewed.
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
     * Constructs a new Report with the specified parameters.
     * Sets status to PENDING and createdAt to current time.
     * 
     * @param userGU UUID of the reporting user
     * @param listingGU UUID of the listing being reported
     * @param reason categorized reason for the report
     * @param message optional detailed explanation
     */
    public Report(UUID userGU, UUID listingGU, ReportReason reason, String message) {
        this.userGU = userGU;
        this.listingGU = listingGU;
        this.reason = reason;
        this.message = message;
        this.status = ReportStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}



