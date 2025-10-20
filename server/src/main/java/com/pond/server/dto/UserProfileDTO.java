// server/src/main/java/com/pond/server/dto/UserProfileDTO.java
package com.pond.server.dto;

import lombok.Getter;

@Getter
public class UserProfileDTO {
    public Long id;
    public String username;
    public String email;
    public String avatar_url;

    public UserProfileDTO(Long id, String username, String email, String avatar_url) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar_url = avatar_url;
    }
}