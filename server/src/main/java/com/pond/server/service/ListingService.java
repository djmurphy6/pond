package com.pond.server.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pond.server.dto.CreateListingRequest;
import com.pond.server.dto.ListingDTO;
import com.pond.server.dto.ListingDetailDTO;
import com.pond.server.dto.ScoredListing;
import com.pond.server.dto.UpdateListingRequest;
import com.pond.server.model.Listing;
import com.pond.server.model.User;
import com.pond.server.repository.ListingRepository;
import com.pond.server.repository.ReportRepository;
import com.pond.server.repository.ResolvedReportRepository;
import com.pond.server.repository.UserFollowingRepository;
import com.pond.server.repository.UserRepository;

/**
 * Service class for managing marketplace listings.
 * Handles CRUD operations, image management, filtering, search, and sold status tracking.
 */
@Service
public class ListingService {
    private final ListingRepository listingRepository;
    private final ImageService imageService;
    private final SupabaseStorage supabaseStorage;
    private final UserRepository userRepository;
    private final UserFollowingRepository userFollowingRepository;
    private final ReportRepository reportRepository;
    private final ResolvedReportRepository resolvedReportRepository;
    
    @Value("${supabase.listing-bucket}")
    private String listingBucket;

    /**
     * Constructs a new ListingService with required dependencies.
     *
     * @param listingRepository the repository for listing data access
     * @param imageService the service for image processing
     * @param supabaseStorage the service for Supabase storage operations
     * @param userRepository the repository for user data access
     * @param userFollowingRepository the repository for user following relationships
     * @param reportRepository the repository for report data access
     * @param resolvedReportRepository the repository for resolved report data access
     */
    public ListingService(ListingRepository listingRepository,
                          ImageService imageService,
                          SupabaseStorage supabaseStorage,
                          UserRepository userRepository,
                          UserFollowingRepository userFollowingRepository,
                          ReportRepository reportRepository,
                          ResolvedReportRepository resolvedReportRepository) {
        this.listingRepository = listingRepository;
        this.imageService = imageService;
        this.supabaseStorage = supabaseStorage;
        this.userRepository = userRepository;
        this.userFollowingRepository = userFollowingRepository;
        this.reportRepository = reportRepository;
        this.resolvedReportRepository = resolvedReportRepository;
    }

    /**
     * Creates a new listing with optional images.
     * Processes and uploads base64 images if provided, or uses provided URLs.
     *
     * @param req the create listing request containing listing details and optional images
     * @param owner the user creating the listing
     * @return the created ListingDTO
     */
    @Transactional
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

    /**
     * Uploads a listing image to Supabase storage.
     * Processes the image (resize and compress) before uploading.
     *
     * @param userGU the UUID of the user who owns the listing
     * @param listingGU the UUID of the listing
     * @param index the image index (1 or 2)
     * @param base64OrDataUrl the base64 encoded image data (with or without data URL prefix)
     * @return the public URL of the uploaded image
     */
    private String uploadListingImage(UUID userGU, UUID listingGU, int index, String base64OrDataUrl) {
        byte[] raw = decodeBase64Image(base64OrDataUrl);
        ImageService.ImageResult img = imageService.process(raw, 2048, 2048, 0.88f);
        String key = "listings/%s/%s/%d-%s.jpg".formatted(userGU, listingGU, index, UUID.randomUUID());
        return supabaseStorage.uploadPublic(listingBucket, key, img.bytes(), img.contentType());
    }

    /**
     * Decodes a base64 image string to bytes.
     * Handles both data URLs (with prefix) and raw base64 strings.
     *
     * @param s the base64 string (with or without data URL prefix)
     * @return the decoded image bytes, or null if input is null
     */
    private byte[] decodeBase64Image(String s) {
        if (s == null) return null;
        int comma = s.indexOf(',');
        String payload = comma >= 0 ? s.substring(comma + 1) : s;
        return java.util.Base64.getDecoder().decode(payload);
    }

    /**
     * Retrieves detailed information about a specific listing.
     * Includes seller username and avatar information.
     *
     * @param id the UUID of the listing
     * @return the detailed listing information
     * @throws RuntimeException if listing not found
     */
    @Transactional(readOnly = true)
    public ListingDetailDTO get(UUID id) {
        String username = null;
        String avatar_url = null;

        Listing l = listingRepository.findById(id).orElseThrow(() -> new RuntimeException("Listing not found"));
        User user = userRepository.findById(l.getUserGU()).orElse(null);
        if (user != null) {
            username = user.getUsername();
            avatar_url = user.getAvatar_url();
        }
        return new ListingDetailDTO(
            l.getListingGU(),
            l.getUserGU(),
            username,
            avatar_url,
            l.getTitle(),
            l.getDescription(),
            l.getPicture1_url(),
            l.getPicture2_url(),
            l.getPrice(),
            l.getCondition(),
            l.getCategory(),
            l.getCreatedAt(),
            l.getSold(),
            l.getSoldTo()
        );
    }

    /**
     * Retrieves all listings in the system.
     *
     * @return a list of all listings
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> all() {
        return listingRepository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Retrieves listings with filtering, sorting, and fuzzy search capabilities.
     * Applies Levenshtein distance algorithm for fuzzy text matching when search query is provided.
     *
     * @param categories list of categories to filter by (null/empty for all)
     * @param minPrice minimum price filter (null for no minimum)
     * @param maxPrice maximum price filter (null for no maximum)
     * @param sortBy field to sort by ("date" or "price", defaults to "date")
     * @param sortOrder sort order ("asc" or "desc", defaults to "desc")
     * @param searchQuery fuzzy search query for listing titles (null/empty for no search)
     * @return a list of filtered and sorted listings (limited to 500 results)
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getFiltered(List<String> categories, Double minPrice, Double maxPrice, String sortBy, String sortOrder, String searchQuery) {
        // Default sort parameters if not provided
        String effectiveSortBy = (sortBy == null || sortBy.isEmpty()) ? "date" : sortBy;
        String effectiveSortOrder = (sortOrder == null || sortOrder.isEmpty()) ? "desc" : sortOrder;
        
        // Convert empty list to null for proper JPA query handling
        List<String> effectiveCategories = (categories != null && !categories.isEmpty()) ? categories : null;
        
        // Trim search query and convert empty to null
        String effectiveSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim().toLowerCase() : null;
        
        // If search query provided, we need entities for fuzzy matching
        if (effectiveSearchQuery != null && !effectiveSearchQuery.isEmpty()) {
            List<Listing> listings = listingRepository.findFiltered(effectiveCategories, minPrice, maxPrice, effectiveSortBy, effectiveSortOrder, null);
            return applyFuzzySearch(listings, effectiveSearchQuery, effectiveSortBy, effectiveSortOrder);
        }
        
        // No search query: directly return DTO projection (avoids entity materialization and mapping)
        return listingRepository.findFilteredDTOWithLimit(
            effectiveCategories, minPrice, maxPrice, effectiveSortBy, effectiveSortOrder,
            PageRequest.of(0, 500)
        );
    }

    /**
     * Retrieves listings with filtering, sorting, search, and pagination.
     * Paginated variant of getFiltered method.
     *
     * @param categories list of categories to filter by (null/empty for all)
     * @param minPrice minimum price filter (null for no minimum)
     * @param maxPrice maximum price filter (null for no maximum)
     * @param sortBy field to sort by ("date" or "price", defaults to "date")
     * @param sortOrder sort order ("asc" or "desc", defaults to "desc")
     * @param searchQuery fuzzy search query for listing titles (null/empty for no search)
     * @param page the page number (zero-based)
     * @param size the page size
     * @return a page of filtered and sorted listings
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getFilteredPaged(List<String> categories, Double minPrice, Double maxPrice, String sortBy, String sortOrder, String searchQuery, int page, int size) {
        String effectiveSortBy = (sortBy == null || sortBy.isEmpty()) ? "date" : sortBy;
        String effectiveSortOrder = (sortOrder == null || sortOrder.isEmpty()) ? "desc" : sortOrder;
        List<String> effectiveCategories = (categories != null && !categories.isEmpty()) ? categories : null;
        String effectiveSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim().toLowerCase() : null;

        if (effectiveSearchQuery != null && !effectiveSearchQuery.isEmpty()) {
            // For fuzzy search, fetch a page of entities then apply fuzzy scoring
            List<Listing> listings = listingRepository.findFilteredWithLimit(
                effectiveCategories, minPrice, maxPrice, effectiveSortBy, effectiveSortOrder, null,
                PageRequest.of(page, size)
            );
            return applyFuzzySearch(listings, effectiveSearchQuery, effectiveSortBy, effectiveSortOrder);
        }

        return listingRepository.findFilteredDTOWithLimit(
            effectiveCategories, minPrice, maxPrice, effectiveSortBy, effectiveSortOrder,
            PageRequest.of(page, size)
        );
    }
    
    /**
     * Applies fuzzy search to a list of listings using Levenshtein distance algorithm.
     * Scores listings based on similarity to search query and filters by 50% similarity threshold.
     *
     * @param listings the list of listings to search through
     * @param searchQuery the search query (already lowercase and trimmed)
     * @param sortBy field to sort by after filtering ("date" or "price")
     * @param sortOrder sort order after filtering ("asc" or "desc")
     * @return a list of listings matching the search query, sorted as specified
     */
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
    
    /**
     * Creates a comparator for sorting listings based on specified criteria.
     *
     * @param sortBy field to sort by ("price" or "date")
     * @param sortOrder sort order ("asc" for ascending, anything else for descending)
     * @return a comparator for sorting Listing objects
     */
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
    
    /**
     * Retrieves all listings owned by a specific user.
     *
     * @param owner the user whose listings to retrieve
     * @return a list of the user's listings
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> mine(User owner) {
        return listingRepository.findByUserGU(owner.getUserGU()).stream().map(this::toDto).toList();
    }

    /**
     * Retrieves all listings owned by a user identified by UUID.
     *
     * @param gu the UUID of the user
     * @return a list of the user's listings
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getUserListings(UUID gu) {
        return listingRepository.findByUserGU(gu).stream().map(this::toDto).toList();
    }

    /**
     * Retrieves listings from users that the current user follows.
     * Supports filtering, sorting, and search just like getFiltered().
     * OPTIMIZED: Uses database query instead of loading all listings into memory.
     *
     * @param currentUser the user requesting the listings
     * @param categories list of categories to filter by (null/empty for all)
     * @param minPrice minimum price filter (null for no minimum)
     * @param maxPrice maximum price filter (null for no maximum)
     * @param sortBy field to sort by ("date" or "price", defaults to "date")
     * @param sortOrder sort order ("asc" or "desc", defaults to "desc")
     * @param searchQuery fuzzy search query for listing titles (null/empty for no search)
     * @return a list of listings from followed users (limited to 500 results)
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getFollowingListings(User currentUser, List<String> categories, 
                                                  Double minPrice, Double maxPrice, 
                                                  String sortBy, String sortOrder, String searchQuery) {
        // Get list of users that current user follows
        List<UUID> followingUserIds = userFollowingRepository.findByFollowerGU(currentUser.getUserGU())
            .stream()
            .map(uf -> uf.getFollowingGU())
            .collect(Collectors.toList());
        
        // If not following anyone, return empty list
        if (followingUserIds.isEmpty()) {
            return List.of();
        }
        
        // Default sort parameters if not provided
        String effectiveSortBy = (sortBy == null || sortBy.isEmpty()) ? "date" : sortBy;
        String effectiveSortOrder = (sortOrder == null || sortOrder.isEmpty()) ? "desc" : sortOrder;
        
        // Convert empty list to null for proper JPA query handling
        List<String> effectiveCategories = (categories != null && !categories.isEmpty()) ? categories : null;
        
        // Trim search query and convert empty to null
        String effectiveSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim().toLowerCase() : null;
        
        // If search query provided, apply fuzzy matching
        if (effectiveSearchQuery != null && !effectiveSearchQuery.isEmpty()) {
            // need entities for fuzzy matching
            List<Listing> filteredListings = listingRepository.findFollowingFiltered(
                followingUserIds,
                effectiveCategories,
                minPrice,
                maxPrice,
                effectiveSortBy,
                effectiveSortOrder
            );
            return applyFuzzySearch(filteredListings, effectiveSearchQuery, effectiveSortBy, effectiveSortOrder);
        }
        
        // No search query: use DTO projection with limit
        return listingRepository.findFollowingFilteredDTOWithLimit(
            followingUserIds,
            effectiveCategories,
            minPrice,
            maxPrice,
            effectiveSortBy,
            effectiveSortOrder,
            PageRequest.of(0, 500)
        );
    }

    /**
     * Retrieves listings from followed users with pagination.
     * Paginated variant of getFollowingListings method.
     *
     * @param currentUser the user requesting the listings
     * @param categories list of categories to filter by (null/empty for all)
     * @param minPrice minimum price filter (null for no minimum)
     * @param maxPrice maximum price filter (null for no maximum)
     * @param sortBy field to sort by ("date" or "price", defaults to "date")
     * @param sortOrder sort order ("asc" or "desc", defaults to "desc")
     * @param searchQuery fuzzy search query for listing titles (null/empty for no search)
     * @param page the page number (zero-based)
     * @param size the page size
     * @return a page of listings from followed users
     */
    @Transactional(readOnly = true)
    public List<ListingDTO> getFollowingListingsPaged(User currentUser, List<String> categories,
                                                      Double minPrice, Double maxPrice,
                                                      String sortBy, String sortOrder, String searchQuery,
                                                      int page, int size) {
        List<UUID> followingUserIds = userFollowingRepository.findByFollowerGU(currentUser.getUserGU())
            .stream()
            .map(uf -> uf.getFollowingGU())
            .collect(Collectors.toList());
        if (followingUserIds.isEmpty()) {
            return List.of();
        }
        String effectiveSortBy = (sortBy == null || sortBy.isEmpty()) ? "date" : sortBy;
        String effectiveSortOrder = (sortOrder == null || sortOrder.isEmpty()) ? "desc" : sortOrder;
        List<String> effectiveCategories = (categories != null && !categories.isEmpty()) ? categories : null;
        String effectiveSearchQuery = (searchQuery != null && !searchQuery.trim().isEmpty()) ? searchQuery.trim().toLowerCase() : null;

        if (effectiveSearchQuery != null && !effectiveSearchQuery.isEmpty()) {
            List<Listing> filteredListings = listingRepository.findFollowingFilteredWithLimit(
                followingUserIds,
                effectiveCategories,
                minPrice,
                maxPrice,
                effectiveSortBy,
                effectiveSortOrder,
                PageRequest.of(page, size)
            );
            return applyFuzzySearch(filteredListings, effectiveSearchQuery, effectiveSortBy, effectiveSortOrder);
        }

        return listingRepository.findFollowingFilteredDTOWithLimit(
            followingUserIds,
            effectiveCategories,
            minPrice,
            maxPrice,
            effectiveSortBy,
            effectiveSortOrder,
            PageRequest.of(page, size)
        );
    }

    /**
     * Updates an existing listing.
     * Admins can edit any listing, regular users can only edit their own.
     * Handles image updates including deletion of old images when replaced.
     *
     * @param id the UUID of the listing to update
     * @param req the update request containing fields to update
     * @param currentUser the user performing the update
     * @return the updated ListingDTO
     * @throws RuntimeException if listing not found or user not authorized
     */
    @Transactional
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

    /**
     * Deletes a listing and all associated data.
     * Admins can delete any listing, regular users can only delete their own.
     * Deletes listing images from storage and associated reports from database.
     *
     * @param id the UUID of the listing to delete
     * @param currentUser the user performing the deletion
     * @throws RuntimeException if listing not found or user not authorized
     */
    @Transactional
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
        
        System.out.println("Deleting listing: " + id);
        System.out.println("Picture 1 URL: " + l.getPicture1_url());
        System.out.println("Picture 2 URL: " + l.getPicture2_url());
        
        // Delete reports and resolved reports before deleting listing
        // (This will be handled by CASCADE DELETE after migration, but keeping it for safety)
        reportRepository.findByListingGU(id).forEach(report -> reportRepository.delete(report));
        resolvedReportRepository.findByListingGU(id).forEach(resolvedReport -> resolvedReportRepository.delete(resolvedReport));
        
        deleteListingImage(l.getPicture1_url());
        deleteListingImage(l.getPicture2_url());
        listingRepository.delete(l);
        System.out.println("Successfully deleted listing: " + id);
    }

    /**
     * Deletes a listing image from Supabase storage.
     * Extracts the storage key from the URL and performs deletion.
     * Logs warnings/errors but does not throw exceptions.
     *
     * @param url the full URL of the image to delete
     */
    private void deleteListingImage(String url){
        if (url == null || url.isBlank()) {
            System.out.println("No image to delete (URL is null or blank)");
            return;
        }

        System.out.println("Attempting to delete listing image: " + url);
        
        String marker = "/storage/v1/object/public/" + listingBucket + "/";
        int idx = url.indexOf(marker);
        if (idx >= 0){
            String key = url.substring(idx + marker.length());
            System.out.println("Extracted listing image key: " + key + " from bucket: " + listingBucket);
            try {
                supabaseStorage.deleteObject(listingBucket, key);
                System.out.println("Successfully deleted listing image from storage");
            } catch (Exception e) {
                System.err.println("ERROR: Failed to delete listing image " + url);
                System.err.println("Key: " + key);
                System.err.println("Bucket: " + listingBucket);
                System.err.println("Exception: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("WARNING: Could not extract key from listing image URL: " + url);
            System.err.println("Expected marker: " + marker);
        }
    }

    /**
     * Toggles the sold status of a listing.
     * Marks listing as sold (with buyer ID) or unsold (clears buyer ID).
     *
     * @param id the UUID of the listing
     * @param soldToId the UUID of the buyer (used when marking as sold)
     * @param owner the user who owns the listing
     * @return the updated ListingDTO
     * @throws RuntimeException if listing not found or not owned by user
     */
    @Transactional
    public ListingDTO toggleSold(UUID id, UUID soldToId, User owner) {
        Listing l = listingRepository.findByListingGUAndUserGU(id, owner.getUserGU())
                .orElseThrow(() -> new RuntimeException("Listing not found or not owned by user"));

        // Toggle the sold status
        l.setSold(!l.getSold());

        // If unmarking as sold, clear the soldTo field
        if (!l.getSold()) {
            l.setSoldTo(null);
        } else {
            l.setSoldTo(soldToId);
        }
        
        l = listingRepository.save(l);
        return toDto(l);
    }
    
    /**
     * Converts a Listing entity to a ListingDTO.
     *
     * @param l the listing entity to convert
     * @return the ListingDTO representation
     */
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
            l.getCreatedAt(),
            l.getSold(),
            l.getSoldTo()
    );
    }
}