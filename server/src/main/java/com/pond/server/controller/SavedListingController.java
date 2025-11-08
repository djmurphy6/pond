package com.pond.server.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pond.server.dto.ListingDTO;
import com.pond.server.dto.SavedListingRequest;
import com.pond.server.model.User;
import com.pond.server.service.SavedListingService;

@RestController
@RequestMapping("/saved-listings")
public class SavedListingController {
    
    private final SavedListingService savedListingService;
    
    public SavedListingController(SavedListingService savedListingService) {
        this.savedListingService = savedListingService;
    }
    
    /**
     * Save a listing
     * POST /saved-listings
     */
    @PostMapping
    public ResponseEntity<?> saveListing(@RequestBody SavedListingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            savedListingService.saveListing(request.getListingGU(), currentUser);
            return ResponseEntity.ok(Map.of("message", "Listing saved successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Unsave a listing
     * DELETE /saved-listings
     */
    @DeleteMapping
    public ResponseEntity<?> unsaveListing(@RequestBody SavedListingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        try {
            savedListingService.unsaveListing(request.getListingGU(), currentUser);
            return ResponseEntity.ok(Map.of("message", "Listing unsaved successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Check if a listing is saved
     * POST /saved-listings/status
     */
    @PostMapping("/status")
    public ResponseEntity<?> checkSavedStatus(@RequestBody SavedListingRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        boolean isSaved = savedListingService.isListingSaved(request.getListingGU(), currentUser);
        return ResponseEntity.ok(Map.of("isSaved", isSaved));
    }
    
    /**
     * Get all saved listings for the current user
     * GET /saved-listings
     */
    @GetMapping
    public ResponseEntity<?> getSavedListings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        List<ListingDTO> savedListings = savedListingService.getSavedListings(currentUser);
        return ResponseEntity.ok(savedListings);
    }
    
    /**
     * Get list of saved listing IDs (lightweight endpoint for checking multiple listings)
     * GET /saved-listings/ids
     */
    @GetMapping("/ids")
    public ResponseEntity<?> getSavedListingIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        
        List<UUID> savedIds = savedListingService.getSavedListingIds(currentUser);
        return ResponseEntity.ok(Map.of("savedListingIds", savedIds));
    }
}

