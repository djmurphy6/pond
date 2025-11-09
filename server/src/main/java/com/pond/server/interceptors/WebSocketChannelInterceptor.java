package com.pond.server.interceptors;

import com.pond.server.service.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public WebSocketChannelInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        if (!accessor.isMutable()) {
            accessor = StompHeaderAccessor.wrap(message);
        }

        StompCommand command = accessor.getCommand();
        if (command == null) return message;

        System.out.println("🔍 STOMP Command: " + command);

        // ✅ Handle the STOMP CONNECT frame for authentication
        if (StompCommand.CONNECT.equals(command)) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            System.out.println("🔑 Authorization header: " + authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String email = jwtService.extractUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    if (jwtService.isAccessTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        accessor.setUser(auth);
                        System.out.println("✅ STOMP CONNECT authenticated user: " + email);
                    } else {
                        System.err.println("❌ Invalid JWT token during STOMP CONNECT");
                    }

                } catch (Exception e) {
                    System.err.println("❌ Error processing JWT: " + e.getMessage());
                }
            } else {
                System.err.println("⚠️ No Authorization header in STOMP CONNECT");
            }
        }

        // For SEND and SUBSCRIBE, maintain authentication context
        if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            if (accessor.getUser() != null) {
                System.out.println("📤 " + command + " from user: " + accessor.getUser().getName());
            } else {
                System.err.println("⚠️ " + command + " without authenticated user");
            }
        }

        return message;
    }
}
