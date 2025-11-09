package com.pond.server.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pond.server.dto.CreateListingRequest;
import com.pond.server.dto.ListingDTO;
import com.pond.server.dto.ListingDetailDTO;
import com.pond.server.dto.ScoredListing;
import com.pond.server.dto.UpdateListingRequest;
import com.pond.server.model.Listing;
import com.pond.server.model.User;
import com.pond.server.repository.ListingRepository;
import com.pond.server.repository.UserRepository;

@Service
public class ListingService {
    private final ListingRepository listingRepository;
    private final ImageService imageService;
    private final SupabaseStorage supabaseStorage;
    private final UserRepository userRepository;
    
    @Value("${supabase.listing-bucket}")
    private String listingBucket;

    public ListingService(ListingRepository listingRepository,
                          ImageService imageService,
                          SupabaseStorage supabaseStorage,
                          UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.imageService = imageService;
        this.supabaseStorage = supabaseStorage;
        this.userRepository = userRepository;
    }

    public ListingDTO create(CreateListingRequest req, User owner) {
        Listing l = new Listing();
        l.setUserGU(owner.getUserGU());
        l.setDescription(req.getDescription());
        l.setPrice(req.getPrice());
        l.setCondition(req.getCondition());
        l.setTitle(req.getTitle());
        l.setCategory(req.getCategory());
        // Save first to get the listingGU
        l = listingRepository.save(l);

        // Prefer images sent as base64 JSON (single request), fallback to provided URLs
        String b1 = req.getPicture1_base64();
        String b2 = req.getPicture2_base64();
        if (b1 != null && !b1.isBlank()) {
            String url1 = uploadListingImage(owner.getUserGU(), l.getListingGU(), 1, b1);
            l.setPicture1_url(url1);
        } else {
            l.setPicture1_url(req.getPicture1_url());
        }
        if (b2 != null && !b2.isBlank()) {
            String url2 = uploadListingImage(owner.getUserGU(), l.getListingGU(), 2, b2);
            l.setPicture2_url(url2);
        } else {
            l.setPicture2_url(req.getPicture2_url());
        }

        // Save the listing with the images
        l = listingRepository.save(l);
        return toDto(l);
    }

    private String uploadListingImage(UUID userGU, UUID listingGU, int index, String base64OrDataUrl) {
        byte[] raw = decodeBase64Image(base64OrDataUrl);
        ImageService.ImageResult img = imageService.process(raw, 2048, 2048, 0.88f);
        String key = "listings/%s/%s/%d-%s.jpg".formatted(userGU, listingGU, index, UUID.randomUUID());
        return supabaseStorage.uploadPublic(listingBucket, key, img.bytes(), img.contentType());
    }

    private byte[] decodeBase64Image(String s) {
        if (s == null) return null;
        int comma = s.indexOf(',');
        String payload = comma >= 0 ? s.substring(comma + 1) : s;
        return java.util.Base64.getDecoder().decode(payload);
    }

    public ListingDetailDTO get(UUID id) {
        Listing l = listingRepository.findById(id).orElseThrow(() -> new RuntimeException("Listing not found"));
        String username = userRepository.findById(l.getUserGU()).map(User::getUsername).orElse(null);
        return new ListingDetailDTO(
            l.getListingGU(),
            l.getUserGU(),
            username,
            l.getTitle(),
            l.getDescription(),
            l.getPicture1_url(),
            l.getPicture2_url(),
            l.getPrice(),
            l.getCondition(),
            l.getCategory(),
            l.getCreatedAt()
        );
    }

    public List<ListingDTO> all() {
        return listingRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<ListingDTO> getFiltered(List<String> categories, Double minPrice, Double maxPrice, String sortBy, String sortOrder, String searchQuery) {
        // Default sort parameters if not provided
        String effectiveSortBy = (sortBy == null || sortBy.isEmpty()) ? "date" : sortBy;
        String effectiveSortOrder = (sortOrder == null || sortOrder.isEmpty()) ? "desc" : sortOrder;
        
        // Convert empty list to null for proper JPA query handling
        List<String> effectiveCategories = (categories != null && !categories.isEmpty()) ? categories : null;
        
        // Trim search query and convert empty to null
        String effectiveSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim().toLowerCase() : null;
        
        // Fetch listings from database with category and price filters (no search query in DB)
        List<Listing> listings = listingRepository.findFiltered(effectiveCategories, minPrice, maxPrice, effectiveSortBy, effectiveSortOrder, null);
        
        // If search query provided, apply fuzzy matching
        if (effectiveSearchQuery != null && !effectiveSearchQuery.isEmpty()) {
            return applyFuzzySearch(listings, effectiveSearchQuery, effectiveSortBy, effectiveSortOrder);
        }
        
        // No search query, return normally sorted results
        return listings.stream()
                .map(this::toDto)
                .toList();
    }
    
    private List<ListingDTO> applyFuzzySearch(List<Listing> listings, String searchQuery, String sortBy, String sortOrder) {
        //Calculator that calculates the similarity between two strings by the number of character replacements required to make the strings the same
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        
        // Score each listing based on similarity to search query
        List<Listing> filteredListings = listings.stream()
                .map(listing -> {
                    String title = listing.getTitle().toLowerCase();
                    
                    // Calculate multiple similarity scores
                    
                    // 1. Exact substring match (highest priority)
                    boolean exactMatch = title.contains(searchQuery);
                    
                    // 2. Word-level matching (check if any word is similar)
                    String[] titleWords = title.split("\\s+"); // ["grey", "shirt"]
                    String[] queryWords = searchQuery.split("\\s+"); // ["gre"]
                    
                    double bestScore = 0.0;
                    
                    // Check each query word against each title word
                    for (String queryWord : queryWords) {
                        for (String titleWord : titleWords) {
                            // Calculate similarity (normalized)
                            int distance = levenshtein.apply(queryWord, titleWord); //distance is number of character replacements needed
                            int maxLength = Math.max(queryWord.length(), titleWord.length()); //which word is longer, the title word or search query word
                            //For "gre" vs "grey" similarity is 1 - (1 / 4) = 0.75 so they are 75% similar
                            double similarity = 1.0 - ((double) distance / maxLength); 
                            bestScore = Math.max(bestScore, similarity); //best score among all words
                        }
                    }
                    
                    // Boost score if exact match found
                    if (exactMatch) {
                        bestScore = Math.max(bestScore, 1.0);
                    }
                    
                    // Also check full title against full query
                    int fullDistance = levenshtein.apply(searchQuery, title);
                    int maxFullLength = Math.max(searchQuery.length(), title.length());
                    double fullSimilarity = 1.0 - ((double) fullDistance / maxFullLength);
                    bestScore = Math.max(bestScore, fullSimilarity);
                    
                    return new ScoredListing(listing, bestScore);
                })
                .filter(scored -> scored.score >= 0.5) // Only include reasonably similar results (50% similarity threshold)
                .map(scored -> scored.listing) // Extract listings after filtering
                .collect(Collectors.toList());
        
        // Apply user's requested sort order
        Comparator<Listing> comparator = getComparator(sortBy, sortOrder);
        filteredListings.sort(comparator);
        
        // Convert to DTOs
        return filteredListings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    // Helper method to get the appropriate comparator based on sort parameters
    private Comparator<Listing> getComparator(String sortBy, String sortOrder) {
        boolean ascending = "asc".equalsIgnoreCase(sortOrder);
        
        Comparator<Listing> comparator;
        if ("price".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Listing::getPrice);
        } else { // default to date
            comparator = Comparator.comparing(Listing::getCreatedAt);
        }
        
        return ascending ? comparator : comparator.reversed();
    }
    

    public List<ListingDTO> mine(User owner) {
        return listingRepository.findByUserGU(owner.getUserGU()).stream().map(this::toDto).toList();
    }

    public ListingDTO update(UUID id, UpdateListingRequest req, User currentUser) {
        // Admins can edit any listing, regular users can only edit their own
        Listing l;
        if (currentUser.getAdmin()) {
            l = listingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));
        } else {
            l = listingRepository.findByListingGUAndUserGU(id, currentUser.getUserGU())
                    .orElseThrow(() -> new RuntimeException("Listing not found or not owned by user"));
        }
        
                // Update text fields
                if (req.getDescription() != null) l.setDescription(req.getDescription());
                if (req.getPrice() != null) l.setPrice(req.getPrice());
                if (req.getCondition() != null) l.setCondition(req.getCondition());
                if (req.getTitle() != null) l.setTitle(req.getTitle());
                if (req.getCategory() != null) l.setCategory(req.getCategory());
                
                // Handle picture1: only treat as base64 if it's a Data URL
                String b1 = req.getPicture1_base64();
                if (b1 != null && b1.startsWith("data:")) {
                    // Delete old image before uploading new one
                    deleteListingImage(l.getPicture1_url());
                    // Upload new image (use listing owner's userGU for storage path)
                    String url1 = uploadListingImage(l.getUserGU(), l.getListingGU(), 1, b1);
                    l.setPicture1_url(url1);
                } else if (req.getPicture1_url() != null) {
                    String url1 = req.getPicture1_url();
                    if (url1.isBlank()) {
                        // Empty string means clear the image
                        deleteListingImage(l.getPicture1_url());
                        l.setPicture1_url(null);
                    } else {
                        // Just update URL if provided (no base64)
                        l.setPicture1_url(url1);
                    }
                }
                // else: no change, keep existing picture1_url
                
                // Handle picture2: only treat as base64 if it's a Data URL
                String b2 = req.getPicture2_base64();
                if (b2 != null && b2.startsWith("data:")) {
                    // Delete old image before uploading new one
                    deleteListingImage(l.getPicture2_url());
                    // Upload new image (use listing owner's userGU for storage path)
                    String url2 = uploadListingImage(l.getUserGU(), l.getListingGU(), 2, b2);
                    l.setPicture2_url(url2);
                } else if (req.getPicture2_url() != null) {
                    String url2 = req.getPicture2_url();
                    if (url2.isBlank()) {
                        // Empty string means clear the image
                        deleteListingImage(l.getPicture2_url());
                        l.setPicture2_url(null);
                    } else {
                        // Just update URL if provided (no base64)
                        l.setPicture2_url(url2);
                    }
                }
                // else: no change, keep existing picture2_url
                
                l = listingRepository.save(l);
                return toDto(l);
    }

    public void delete(UUID id, User currentUser) {
        // Admins can delete any listing, regular users can only delete their own
        Listing l;
        if (currentUser.getAdmin()) {
            l = listingRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Listing not found"));
        } else {
            l = listingRepository.findByListingGUAndUserGU(id, currentUser.getUserGU())
                    .orElseThrow(() -> new RuntimeException("Listing not found or not owned by user"));
        }
        deleteListingImage(l.getPicture1_url());
        deleteListingImage(l.getPicture2_url());
        listingRepository.delete(l);
    }

        private void deleteListingImage(String url){
        if (url == null || url.isBlank()) return;

        String marker = "/storage/v1/object/public/" + listingBucket + "/";
        int idx = url.indexOf(marker);
        if (idx >= 0){
            String key = url.substring(idx + marker.length());
            try {
                supabaseStorage.deleteObject(listingBucket, key);
            } catch (Exception e) {
                System.err.println("Warning: Failed to delete old image " + url + ": " + e.getMessage());
            }
        }
    }
    

    private ListingDTO toDto(Listing l) {
        return new ListingDTO(
            l.getListingGU(),
            l.getUserGU(),
            l.getTitle(),
            l.getDescription(),
            l.getPicture1_url(),
            l.getPicture2_url(),
            l.getPrice(),
            l.getCondition(),
            l.getCategory(),
            l.getCreatedAt()
    );
    }
}