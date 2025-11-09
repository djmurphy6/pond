package com.pond.server.enums;

public enum ReportStatus {
    PENDING,      // Initial state
    UNDER_REVIEW, // Admin is investigating
    RESOLVED,     // Issue resolved, listing kept
    DISMISSED,    // Report was invalid
    LISTING_REMOVED // Listing was taken down
}
