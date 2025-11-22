package com.pond.server.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class MarkListingAsSoldRequest {
    private Boolean sold;
    @JsonProperty("soldTo")
    private UUID soldTo; // UUID of the buyer (optional - can be null if just marking as sold/unsold)
}

