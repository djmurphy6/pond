package com.pond.server.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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

import jakarta.servlet.http.HttpServletRequest;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final UserDetailsService userDetailsService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDTO registerUserDTO) {
            User registeredUser = authenticationService.signup(registerUserDTO);
            return ResponseEntity.ok()
                    .header("Success-message", "Account created successfully")
                    .body(registeredUser);
        
    }

    // TODO: Refresh the refresh Token when refresh is called
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {

        String refresh = jwtService.extractRefreshTokenFromRequest(request)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        String userEmail = jwtService.extractUsername(refresh);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (!jwtService.isRefreshTokenValid(refresh, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
}

    @PostMapping("/login")
    public ResponseEntity<?> Authenticate(@RequestBody LoginUserDTO loginUserDTO) {

            User authenticatedUser = authenticationService.authentication(loginUserDTO);

            String refreshToken = jwtService.generateToken(authenticatedUser);
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true) // for local HTTP dev, you can temporarily set false
                    .path("/")
                    .maxAge(Duration.ofMillis(jwtService.getExpirationTime()))
                    .sameSite("None") // for same-site local HTTP, use "Lax" instead
                    .build();

            String accessToken = jwtService.generateAccessToken(authenticatedUser);
            LoginResponse loginResponse = new LoginResponse(accessToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(loginResponse);



    }

        @PostMapping("/verify")
        public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO verifyUserDTO) {
        authenticationService.verifyUser(verifyUserDTO);
        return ResponseEntity.ok(Map.of("message", "Account verified successfully."));
        }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Expire the refresh cookie
        ResponseCookie expired = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true) // for localhost over HTTP use false and sameSite("Lax")
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expired.toString())
                .body(Map.of("message", "Logged out"));
    }
    // TODO: add the email resend code functionality here and in all areas
    // @PostMapping("/resend")
    // public ResponseEntity
}