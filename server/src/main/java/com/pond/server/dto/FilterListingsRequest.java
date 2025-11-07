package com.pond.server.dto;

import java.util.List;

import lombok.Getter;

@Getter
public class FilterListingsRequest {
    private List<String> categories;
    private Double minPrice;
    private Double maxPrice;
    private String sortBy;
    private String sortOrder;
}

