package com.pond.server.responses;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {
    private Long id;
    private String email;
    private String username;
    private boolean enabled;
    private String message;
}