// server/src/main/java/com/pond/server/dto/UserProfileDTO.java
package com.pond.server.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class UserProfileDTO {
    @JsonProperty("userGU")
    private final UUID UserGU;
    public String username;
    public String email;
    public String avatar_url;
    public String bio;
    public Boolean admin;

    public UserProfileDTO(UUID UserGU, String username, String email, String avatar_url, String bio, Boolean admin) {
        this.UserGU = UserGU;
        this.username = username;
        this.email = email;
        this.avatar_url = avatar_url;
        this.bio = bio;
        this.admin = admin;
    }
}