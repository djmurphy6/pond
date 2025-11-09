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

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_gu", updatable = false, nullable = false)
    private UUID reportGU;

    @Column(name = "user_gu", nullable = false)
    private UUID userGU;  // User who filed the report

    @Column(name = "listing_gu", nullable = false)
    private UUID listingGU;  // Listing being reported

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;

    @Column(name = "message", length = 1000)
    private String message;  // Optional additional details

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_by_admin_gu")
    private UUID reviewedByAdminGU;  // Admin who reviewed it

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;  // Admin can add notes when reviewing

    public Report(UUID userGU, UUID listingGU, ReportReason reason, String message) {
        this.userGU = userGU;
        this.listingGU = listingGU;
        this.reason = reason;
        this.message = message;
        this.status = ReportStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}



