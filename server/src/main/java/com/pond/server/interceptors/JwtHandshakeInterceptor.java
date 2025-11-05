package com.pond.server.interceptors;

import org.springframework.stereotype.Component;
import com.pond.server.service.JwtService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;


@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor{

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtHandshakeInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        System.out.println("🔗 WebSocket handshake initiated");

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

            // Try to get token from query parameter (common for WebSocket)
            String token = servletRequest.getServletRequest().getParameter("token");

            // If not in query param, check Authorization header
            if (token == null || token.isEmpty()) {
                String authHeader = servletRequest.getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }

            System.out.println("Token found: " + (token != null ? "Yes" : "No"));

            if (token != null && !token.isEmpty()) {
                try {
                    String userEmail = jwtService.extractUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                    // Validate token (try access token first, then refresh token)
                    boolean isValid = false;

                    if (jwtService.isAccessTokenValid(token, userDetails)) {
                        isValid = true;
                        System.out.println("✅ Access token valid for: " + userEmail);
                    } else if (jwtService.isRefreshTokenValid(token, userDetails)) {
                        isValid = true;
                        System.out.println("✅ Refresh token valid for: " + userEmail);
                    }

                    if (isValid) {
                        // Store user email in WebSocket session attributes
                        attributes.put("userEmail", userEmail);
                        attributes.put("userDetails", userDetails);
                        System.out.println("✅ User authenticated: " + userEmail);
                        return true;
                    } else {
                        System.err.println("❌ Token validation failed");
                        return false;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Token processing error: " + e.getMessage());
                    return false;
                }
            } else {
                System.err.println("❌ No token provided");
                // Allow connection without auth for testing - change to false in production
                return true;
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        System.out.println("✅ WebSocket handshake completed");
    }

}
