package com.pond.server.model;

import com.pond.server.enums.ReviewType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name ="reviewer_gu", nullable = false)
    private UUID reviewerGu;

    @Column(name ="reviewee_gu", nullable = false)
    private UUID revieweeGu;

    // This is nullable since later on I want to add functionality
    // for Profile and conversation reviews
    @Column(name = "listing_gu", updatable = false, nullable = false)
    private UUID listingGU;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", updatable = true, nullable = false, columnDefinition = "VARCHAR(500)")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false)
    private ReviewType reviewType;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name="updatedAt")
    private LocalDateTime updatedAt;

    public Review(){
        this.timestamp = LocalDateTime.now();
    }

}
