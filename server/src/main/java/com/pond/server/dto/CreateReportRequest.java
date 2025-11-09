package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class CreateReportRequest {
    @JsonProperty("listingGU")
    private String listingGU;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("message")
    private String message;
}