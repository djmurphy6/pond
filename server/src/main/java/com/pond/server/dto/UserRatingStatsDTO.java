package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRatingStatsDTO {

    @JsonProperty("userGu")
    private UUID userGu;

    @JsonProperty("averageRating")
    private Double averageRating;

    @JsonProperty("totalReviews")
    private Long totalReviews;
}
