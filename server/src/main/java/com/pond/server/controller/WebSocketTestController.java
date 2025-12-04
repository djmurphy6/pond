package com.pond.server.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for WebSocket connectivity testing.
 * Provides a simple ping endpoint to verify WebSocket infrastructure is accessible.
 */
@RestController
@RequestMapping("/ws-test")
public class WebSocketTestController {
    
    /**
     * Simple ping endpoint to verify WebSocket endpoint accessibility.
     *
     * @return ResponseEntity with confirmation message
     */
    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(Map.of("message", "WebSocket endpoint is accessible"));
    }

}
