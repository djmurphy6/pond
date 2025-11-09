package com.pond.server.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pond.server.enums.ReportReason;
import com.pond.server.enums.ReportStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportDTO {
    @JsonProperty("reportGU")
    private String reportGU;
    
    @JsonProperty("userGU")
    private String userGU;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("listingGU")
    private String listingGU;
    
    @JsonProperty("listingTitle")
    private String listingTitle;
    
    @JsonProperty("reason")
    private ReportReason reason;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("status")
    private ReportStatus status;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("reviewedByAdminGU")
    private String reviewedByAdminGU;
    
    @JsonProperty("reviewedAt")
    private LocalDateTime reviewedAt;
    
    @JsonProperty("adminNotes")
    private String adminNotes;
}