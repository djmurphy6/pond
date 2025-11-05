package com.pond.server.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pond.server.dto.CreateListingRequest;
import com.pond.server.dto.ListingDTO;
import com.pond.server.dto.UpdateListingRequest;
import com.pond.server.model.Listing;
import com.pond.server.model.User;
import com.pond.server.repository.ListingRepository;

@Service
public class ListingService {
    private final ListingRepository listingRepository;
    private final ImageService imageService;
    private final SupabaseStorage supabaseStorage;
    
    @Value("${supabase.listing-bucket}")
    private String listingBucket;

    public ListingService(ListingRepository listingRepository,
                          ImageService imageService,
                          SupabaseStorage supabaseStorage) {
        this.listingRepository = listingRepository;
        this.imageService = imageService;
        this.supabaseStorage = supabaseStorage;
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

    public ListingDTO get(UUID id) {
        Listing l = listingRepository.findById(id).orElseThrow(() -> new RuntimeException("Listing not found"));
        return toDto(l);
    }

    public List<ListingDTO> all() {
        return listingRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<ListingDTO> mine(User owner) {
        return listingRepository.findByUserGU(owner.getUserGU()).stream().map(this::toDto).toList();
    }

    public ListingDTO update(UUID id, UpdateListingRequest req, User owner) {
        Listing l = listingRepository.findByListingGUAndUserGU(id, owner.getUserGU())
                .orElseThrow(() -> new RuntimeException("Listing not found or not owned by user"));
        
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
                    // Upload new image
                    String url1 = uploadListingImage(owner.getUserGU(), l.getListingGU(), 1, b1);
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
                    // Upload new image
                    String url2 = uploadListingImage(owner.getUserGU(), l.getListingGU(), 2, b2);
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

    public void delete(UUID id, User owner) {
        Listing l = listingRepository.findByListingGUAndUserGU(id, owner.getUserGU())
                .orElseThrow(() -> new RuntimeException("Listing not found or not owned by user"));
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
            l.getCategory()
    );
    }
}