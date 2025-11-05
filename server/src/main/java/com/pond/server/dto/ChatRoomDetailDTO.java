package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// GET A SPECIFiC CHAT ROOM
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
    private Double listingPrice;

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
