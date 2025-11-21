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

@RestController
@RequestMapping("/listings")
public class ListingController {
    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateListingRequest req) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        ListingDTO dto = listingService.create(req, currentUser);
        return ResponseEntity.ok(dto);
        
    }

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

    @GetMapping
    public ResponseEntity<?> all(@RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "2") int size) {
        // Return all listings with default sorting (date desc)
        List<ListingDTO> list = listingService.getFilteredPaged(null, null, null, "date", "desc", null, page, size);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/me")
    public ResponseEntity<?> mine() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(listingService.mine(currentUser));
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") UUID id) {return ResponseEntity.ok(listingService.get(id));}

    @GetMapping("user/{id}")
    public ResponseEntity<?> getByUser(@PathVariable("id") UUID userGU) {return ResponseEntity.ok(listingService.getUserListings(userGU));}

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody UpdateListingRequest req) {
        
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            return ResponseEntity.ok(listingService.update(id, req, currentUser));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            listingService.delete(id, currentUser);
            return ResponseEntity.ok(Map.of("result", "Success"));
    }
}