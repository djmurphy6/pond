package com.pond.server.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ListingDetailDTO {
    @JsonProperty("listingGU")
    private final UUID listinggu;
    @JsonProperty("userGU")
    private final UUID usergu;
    private final String username;
    private final String avatar_url;
    private String title;
    private String description;
    @JsonProperty("picture1_url")
    private String picture1_url;
    @JsonProperty("picture2_url")
    private String picture2_url;
    private Double price;
    private String condition;
    private String category;
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}

