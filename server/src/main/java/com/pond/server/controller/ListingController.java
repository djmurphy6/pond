package com.pond.server.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pond.server.dto.CreateListingRequest;
import com.pond.server.dto.FilterListingsRequest;
import com.pond.server.dto.ListingDTO;
import com.pond.server.dto.UpdateListingRequest;
import com.pond.server.model.User;
import com.pond.server.service.ListingService;

/**
 * REST controller for listing management operations.
 * Handles listing creation, retrieval, filtering, updates, and deletion.
 */
@RestController
@RequestMapping("/listings")
public class ListingController {
    private final ListingService listingService;

    /**
     * Constructs a new ListingController with required dependencies.
     *
     * @param listingService the service for listing operations
     */
    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    /**
     * Creates a new listing for the authenticated user.
     * Handles both image uploads (base64) and image URLs.
     *
     * @param req the create listing request with listing details and optional images
     * @return ResponseEntity with the created listing
     */
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateListingRequest req) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        ListingDTO dto = listingService.create(req, currentUser);
        return ResponseEntity.ok(dto);
        
    }

    /**
     * Retrieves listings with filtering, sorting, and pagination.
     * Supports fuzzy search, category filtering, price range, and sorting.
     *
     * @param req the filter request with filter criteria
     * @param page the page number (zero-based, default 0)
     * @param size the page size (default 2)
     * @return ResponseEntity with filtered and paginated listings
     */
    @PostMapping("/filter")
    public ResponseEntity<?> filter(@RequestBody FilterListingsRequest req,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "size", defaultValue = "2") int size) {
        List<ListingDTO> list = listingService.getFilteredPaged(
            req.getCategories(), 
            req.getMinPrice(), 
            req.getMaxPrice(), 
            req.getSortBy(), 
            req.getSortOrder(),
            req.getSearchQuery(),
            page,
            size
        );
        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves all listings with pagination.
     * Uses default sorting by date (descending).
     *
     * @param page the page number (zero-based, default 0)
     * @param size the page size (default 2)
     * @return ResponseEntity with paginated listings
     */
    @GetMapping
    public ResponseEntity<?> all(@RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "2") int size) {
        // Return all listings with default sorting (date desc)
        List<ListingDTO> list = listingService.getFilteredPaged(null, null, null, "date", "desc", null, page, size);
        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves all listings owned by the authenticated user.
     *
     * @return ResponseEntity with user's listings or 401 if unauthorized
     */
    @GetMapping("/me")
    public ResponseEntity<?> mine() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(listingService.mine(currentUser));
    }

    /**
     * Retrieves listings from users that the authenticated user follows.
     * Supports filtering, sorting, and pagination.
     *
     * @param req the filter request with filter criteria
     * @param page the page number (zero-based, default 0)
     * @param size the page size (default 24)
     * @return ResponseEntity with filtered and paginated listings from followed users or 401 if unauthorized
     */
    @PostMapping("/following")
    public ResponseEntity<?> following(@RequestBody FilterListingsRequest req,
                                       @RequestParam(name = "page", defaultValue = "0") int page,
                                       @RequestParam(name = "size", defaultValue = "24") int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        
        List<ListingDTO> list = listingService.getFollowingListingsPaged(
            currentUser,
            req.getCategories(), 
            req.getMinPrice(), 
            req.getMaxPrice(), 
            req.getSortBy(), 
            req.getSortOrder(),
            req.getSearchQuery(),
            page,
            size
        );
        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves detailed information about a specific listing.
     *
     * @param id the UUID of the listing
     * @return ResponseEntity with listing details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") UUID id) {return ResponseEntity.ok(listingService.get(id));}

    /**
     * Retrieves all listings owned by a specific user.
     *
     * @param userGU the UUID of the user
     * @return ResponseEntity with user's listings
     */
    @GetMapping("user/{id}")
    public ResponseEntity<?> getByUser(@PathVariable("id") UUID userGU) {return ResponseEntity.ok(listingService.getUserListings(userGU));}

    /**
     * Updates an existing listing.
     * Regular users can only update their own listings, admins can update any listing.
     * Supports updating text fields and images.
     *
     * @param id the UUID of the listing to update
     * @param req the update request with new listing data
     * @return ResponseEntity with updated listing
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody UpdateListingRequest req) {
        
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            return ResponseEntity.ok(listingService.update(id, req, currentUser));

    }

    /**
     * Toggles the sold status of a listing.
     * Marks listing as sold with buyer ID or unsold (clearing buyer ID).
     *
     * @param id the UUID of the listing
     * @param soldToId the UUID of the buyer (when marking as sold)
     * @return ResponseEntity with updated listing or 401 if unauthorized
     */
    @PostMapping("/{id}/sold/{soldToId}")
    public ResponseEntity<?> toggleSold(@PathVariable("id") UUID id, @PathVariable("soldToId") UUID soldToId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(listingService.toggleSold(id, soldToId, currentUser));
    }

    /**
     * Deletes a listing and all associated data.
     * Regular users can only delete their own listings, admins can delete any listing.
     * Removes images from storage and associated reports.
     *
     * @param id the UUID of the listing to delete
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            listingService.delete(id, currentUser);
            return ResponseEntity.ok(Map.of("result", "Success"));
    }
}