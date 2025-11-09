package com.pond.server.dto;

import com.pond.server.model.Listing;

// Helper class to hold listing with its similarity score
public class ScoredListing {
    public Listing listing;
    public double score;
    
    public ScoredListing(Listing listing, double score) {
        this.listing = listing;
        this.score = score;
    }
}
