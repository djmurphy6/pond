package com.pond.server.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id; // Database internal key

    @Column(name = "room_id", unique = true, nullable = false)
    private String roomId; // Room_Id is generated with listing_{listingGU}_buyer{buyerGU}

    @Column(name = "listing_gu", nullable = false)
    private UUID listingGU;

    @Column(name = "seller_gu", nullable = false)
    private UUID sellerGU;

    @Column(name = "buyer_gu", nullable = false)
    private UUID buyerGU;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    public ChatRoom(){
        this.createdAt = LocalDateTime.now();
    }
}
