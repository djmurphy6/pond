package com.pond.server.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO for sending messages via WebSocket
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("content")
    private String content;
}

