package com.pond.server.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {

    @JsonProperty("reviewGU")
    private UUID reviewGU;

    @JsonProperty("reviewerGu")
    private UUID reviewerGu;

    @JsonProperty("revieweeGu")
    private UUID revieweeGu;

    @JsonProperty("rating")
    private Integer rating;

    @JsonProperty("comment")
    private String comment;

    //TODO: Add the logic for diff types of reviews
//    @JsonProperty("reviewType")
//    private ReviewType reviewType;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("reviewerName")
    private String reviewerName;

    @JsonProperty("reviewerAvatar")
    private String reviewerAvatar;
}
