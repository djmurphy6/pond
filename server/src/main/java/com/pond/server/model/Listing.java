package com.pond.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "listings")
@Getter
@Setter
public class Listing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "listinggu", updatable = false, nullable = false)
    private UUID listingGU;

    @Column(name = "usergu", nullable = false)
    private UUID userGU;

    @Column(name = "description")
    private String description;

    @Column(name = "title")
    private String title;

    @Column(name = "picture1_url")
    private String picture1_url;

    @Column(name = "picture2_url")
    private String picture2_url;

    @Column(name = "price")
    private Double price;

    @Column(name = "condition")
    private String condition;

    @Column(name = "category")
    private String category;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Listing() {
        this.createdAt = LocalDateTime.now();
    }
}
