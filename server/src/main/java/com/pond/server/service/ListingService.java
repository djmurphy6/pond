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
        String key = "listings/%s/%s/%d.jpg".formatted(userGU, listingGU, index);
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
        if (req.getDescription() != null) l.setDescription(req.getDescription());
        if (req.getPicture1_url() != null) l.setPicture1_url(req.getPicture1_url());
        if (req.getPicture2_url() != null) l.setPicture2_url(req.getPicture2_url());
        if (req.getPrice() != null) l.setPrice(req.getPrice());
        if (req.getCondition() != null) l.setCondition(req.getCondition());
        if (req.getTitle() != null) l.setTitle(req.getTitle());
        l = listingRepository.save(l);
        return toDto(l);
    }

    public void delete(UUID id, User owner) {
        Listing l = listingRepository.findByListingGUAndUserGU(id, owner.getUserGU())
                .orElseThrow(() -> new RuntimeException("Listing not found or not owned by user"));
        listingRepository.delete(l);
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
            l.getCondition()
    );
    }
}