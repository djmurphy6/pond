package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

// DTO for notification
@Getter
@AllArgsConstructor
public class NotificationDTO {
    @JsonProperty("message")
    private String message;

    @JsonProperty("roomId")
    private String roomId;
}
