package com.pond.server.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pond.server.dto.LoginUserDTO;
import com.pond.server.dto.RegisterUserDTO;
import com.pond.server.dto.VerifyUserDTO;
import com.pond.server.model.User;
import com.pond.server.responses.LoginResponse;
import com.pond.server.service.AuthenticationService;
import com.pond.server.service.JwtService;


@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;


    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService){
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;

    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDTO registerUserDTO) {
        try {
            User registeredUser = authenticationService.signup(registerUserDTO);
            return ResponseEntity.ok()
                    .header("Success-message","Account created successfully")
                    .body(registeredUser);
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDTO loginUserDTO){
        try {
            User authenticatedUser = authenticationService.authentication(loginUserDTO);

            
            String jwtToken = jwtService.generateToken(authenticatedUser);
            ResponseCookie cookie = ResponseCookie.from("accessToken", jwtToken)
            .httpOnly(true)
            .secure(true) // for local HTTP dev, you can temporarily set false
            .path("/")
            .maxAge(jwtService.getExpirationTime())
            .sameSite("None") // for same-site local HTTP, use "Lax" instead
            .build();

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged in"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e);
        }

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO verifyUserDTO){
        try{
            authenticationService.verifyUser(verifyUserDTO);
            return ResponseEntity.ok("Account verified successfully.");
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
// TODO: add the email resend code functionality here and in all areas
//    @PostMapping("/resend")
//    public ResponseEntity
}