package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pond.server.enums.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {


    @JsonProperty("id")
    private UUID id;

    @JsonProperty("reviewerGu")
    private UUID reviewerGu;

    @JsonProperty("revieweeGu")
    private UUID revieweeGu;

    @JsonProperty("listingGU")
    private UUID listingGU;

    @JsonProperty("rating")
    private Integer rating;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("reviewType")
    private ReviewType reviewType;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
