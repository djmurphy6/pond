package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

// DTO for message responses
@Getter
@AllArgsConstructor
public class MessageResponseDTO {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("senderGU")
    private UUID senderGU;

    @JsonProperty("content")
    private String content;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("isRead")
    private boolean isRead;
}
