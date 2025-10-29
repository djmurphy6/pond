package com.pond.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class UploadAvatarRequest {
    @JsonProperty("avatar_base64")
    private String avatar_base64;
}