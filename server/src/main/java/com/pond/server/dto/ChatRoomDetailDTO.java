package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// DTO for chat room details with listing info
@Getter
@AllArgsConstructor
public class ChatRoomDetailDTO {
    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("listingGU")
    private UUID listingGU;

    @JsonProperty("listingTitle")
    private String listingTitle;

    @JsonProperty("listingPrice")
    private Integer listingPrice;

    @JsonProperty("listingImage")
    private String listingImage;

    @JsonProperty("otherUserGU")
    private UUID otherUserGU;

    @JsonProperty("otherUsername")
    private String otherUsername;

    @JsonProperty("otherUserAvatar")
    private String otherUserAvatar;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("lastMessageAt")
    private LocalDateTime lastMessageAt;
}
