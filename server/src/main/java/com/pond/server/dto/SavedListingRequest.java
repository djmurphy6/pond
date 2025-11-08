package com.pond.server.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedListingRequest {
    private UUID listingGU;
}

