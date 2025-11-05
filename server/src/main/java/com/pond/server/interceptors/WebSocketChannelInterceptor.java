
package com.pond.server.interceptors;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (!accessor.isMutable()) {
            accessor = StompHeaderAccessor.wrap(message);
        }

        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        System.out.println("🔍 STOMP Command: " + command);

        // For CONNECT command, retrieve user from session attributes
        if (StompCommand.CONNECT.equals(command)) {
            String userEmail = (String) accessor.getSessionAttributes().get("userEmail");
            UserDetails userDetails = (UserDetails) accessor.getSessionAttributes().get("userDetails");

            System.out.println("📧 User from session: " + userEmail);

            if (userEmail != null && userDetails != null) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                accessor.setUser(auth);
                System.out.println("✅ User authenticated in STOMP: " + userEmail);
            }
        }

        // For SEND and SUBSCRIBE commands, maintain authentication
        if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            // Authentication should already be set from CONNECT
            if (accessor.getUser() != null) {
                System.out.println("📤 " + command + " from user: " + accessor.getUser().getName());
            } else {
                System.err.println("⚠️ " + command + " without authenticated user");
            }
        }

        return message;
    }
}