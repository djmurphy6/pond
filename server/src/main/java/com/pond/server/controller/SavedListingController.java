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

/**
 * REST controller for saved listing (favorites) management.
 * Handles saving, unsaving, and retrieving user's saved listings.
 */
@RestController
@RequestMapping("/saved-listings")
public class SavedListingController {
    
    private final SavedListingService savedListingService;
    
    /**
     * Constructs a new SavedListingController with required dependencies.
     *
     * @param savedListingService the service for saved listing operations
     */
    public SavedListingController(SavedListingService savedListingService) {
        this.savedListingService = savedListingService;
    }
    
    /**
     * Saves a listing to the authenticated user's favorites.
     *
     * @param request the request containing listing UUID to save
     * @return ResponseEntity with success message or error
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
     * Removes a listing from the authenticated user's favorites.
     *
     * @param request the request containing listing UUID to unsave
     * @return ResponseEntity with success message or error
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
     * Checks if a specific listing is saved by the authenticated user.
     *
     * @param request the request containing listing UUID to check
     * @return ResponseEntity with saved status or 401 if unauthorized
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
     * Retrieves all saved listings for the authenticated user with full details.
     *
     * @return ResponseEntity with list of saved listings or 401 if unauthorized
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
     * Retrieves a list of saved listing IDs (lightweight endpoint).
     * Useful for checking save status of multiple listings at once.
     *
     * @return ResponseEntity with list of UUIDs or 401 if unauthorized
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

