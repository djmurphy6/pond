package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// LiST ALL ACTIVE CHAT ROOMS
@Getter
@AllArgsConstructor
public class ChatRoomListDTO {
    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("listingGU")
    private UUID listingGU;

    @JsonProperty("listingTitle")
    private String listingTitle;

    @JsonProperty("listingImage")
    private String listingImage;

    @JsonProperty("otherUserGU")
    private UUID otherUserGU;

    @JsonProperty("otherUsername")
    private String otherUsername;

    @JsonProperty("otherUserAvatar")
    private String otherUserAvatar;

    @JsonProperty("lastMessage")
    private String lastMessage;

    @JsonProperty("lastMessageAt")
    private LocalDateTime lastMessageAt;

    @JsonProperty("unreadCount")
    private long unreadCount;
}
