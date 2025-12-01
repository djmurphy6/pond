package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewRequest {

    @JsonProperty("reviewGU")
    private Integer reviewGu;

    @JsonProperty("rating")
    private Integer rating;

    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    @JsonProperty("comment")
    private String comment;

}
