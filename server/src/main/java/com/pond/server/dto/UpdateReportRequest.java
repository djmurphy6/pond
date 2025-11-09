package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class UpdateReportRequest {
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("adminNotes")
    private String adminNotes;
}