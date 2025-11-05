package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateListingRequest {
    private String description;
    @JsonProperty("picture1_url")
    private String picture1_url;
    @JsonProperty("picture2_url")
    private String picture2_url;
    private Double price;
    private String condition;
    private String title;
    private String category;
    private String picture1_base64;
    private String picture2_base64;
}
