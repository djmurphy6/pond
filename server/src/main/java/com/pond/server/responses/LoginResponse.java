package com.pond.server.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter

@NoArgsConstructor
public class LoginResponse {
    private String token;
    private long expiresIn;

   public LoginResponse(String token, long expiresIn){
       this.token = token;
       this.expiresIn =expiresIn;
   }
}
