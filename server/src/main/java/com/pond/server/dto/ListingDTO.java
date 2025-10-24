package com.pond.server.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ListingDTO {
    @JsonProperty("listinggu")
    private final UUID listinggu;
    @JsonProperty("usergu")
    private final UUID usergu;
    private String title;
    private String description;
    @JsonProperty("picture1_url")
    private String picture1_url;
    @JsonProperty("picture2_url")
    private String picture2_url;
    private Integer price;
    private String condition;
    
}