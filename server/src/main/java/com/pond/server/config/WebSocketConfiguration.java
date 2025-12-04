package com.pond.server.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.pond.server.interceptors.JwtHandshakeInterceptor;
import com.pond.server.interceptors.WebSocketChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final WebSocketChannelInterceptor webSocketChannelInterceptor;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    // Set FRONTEND_URL environment variable for production
    // Set ADDITIONAL_ORIGINS for comma-separated list of extra origins
    @Value("${FRONTEND_URL:}")
    private String frontendUrl;
    
    @Value("${ADDITIONAL_ORIGINS:}")
    private String additionalOrigins;

    public WebSocketConfiguration(WebSocketChannelInterceptor webSocketChannelInterceptor,
                                  JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.webSocketChannelInterceptor = webSocketChannelInterceptor;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // Build allowed origins list
        List<String> allowedOrigins = new ArrayList<>();
        
        // Add production frontend URL if provided
        if (frontendUrl != null && !frontendUrl.isEmpty() && !frontendUrl.equals("http://localhost:3000")) {
            allowedOrigins.add(frontendUrl);
        }
        
        // Add additional origins from environment variable (comma-separated)
        if (additionalOrigins != null && !additionalOrigins.isEmpty()) {
            String[] origins = additionalOrigins.split(",");
            for (String origin : origins) {
                String trimmed = origin.trim();
                if (!trimmed.isEmpty()) {
                    allowedOrigins.add(trimmed);
                }
            }
        }
        
        // Always allow localhost for development/testing
        allowedOrigins.add("http://localhost:3000");
        allowedOrigins.add("http://localhost:5173");
        
        // Production/test URLs
        allowedOrigins.add("https://pond-kohl.vercel.app");
        
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.toArray(new String[0]))
//                .addInterceptors(jwtHandshakeInterceptor) //TODO: for testing backend only without frontend
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration){
        registration.interceptors(webSocketChannelInterceptor);
    }
}