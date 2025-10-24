package com.pond.server.service;

import java.util.List;
import java.util.UUID;

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

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public ListingDTO create(CreateListingRequest req, User owner) {
        Listing l = new Listing();
        l.setUserGU(owner.getUserGU());
        l.setDescription(req.getDescription());
        l.setPicture1_url(req.getPicture1_url());
        l.setPicture2_url(req.getPicture2_url());
        l.setPrice(req.getPrice());
        l.setCondition(req.getCondition());
        l = listingRepository.save(l);
        return toDto(l);
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
                l.getDescription(),
                l.getPicture1_url(),
                l.getPicture2_url(),
                l.getPrice(),
                l.getCondition()
        );
    }
}