package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class UpdateListingRequest {
    private String description;
    @JsonProperty("picture1_url")
    private String picture1_url;
    @JsonProperty("picture2_url")
    private String picture2_url;
    private Integer price;
    private String condition;
    private String title;
}