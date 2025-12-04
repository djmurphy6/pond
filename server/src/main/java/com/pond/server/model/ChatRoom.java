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

/**
 * Entity representing a chat room between a buyer and seller in the Pond marketplace.
 * 
 * <p>Chat rooms are created when a potential buyer initiates a conversation with
 * a seller about a specific listing. The room ID follows the format:
 * {@code listing_{listingGU}_buyer_{buyerGU}} to ensure uniqueness per buyer-listing pair.</p>
 * 
 * <p>The {@link #lastMessageAt} field is updated whenever a new message is sent,
 * allowing for sorting chat rooms by recent activity.</p>
 * 
 * @author Pond Team
 */
@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
public class ChatRoom {

    /**
     * Unique identifier for the chat room (UUID).
     * Database internal key generated automatically.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Application-level unique identifier for the chat room.
     * Generated with format: listing_{listingGU}_buyer_{buyerGU}
     * Used for WebSocket subscriptions and message routing.
     */
    @Column(name = "room_id", unique = true, nullable = false)
    private String roomId;

    /**
     * UUID of the listing this chat room is associated with.
     */
    @Column(name = "listing_gu", nullable = false)
    private UUID listingGU;

    /**
     * UUID of the seller (listing owner).
     */
    @Column(name = "seller_gu", nullable = false)
    private UUID sellerGU;

    /**
     * UUID of the buyer (potential purchaser).
     */
    @Column(name = "buyer_gu", nullable = false)
    private UUID buyerGU;

    /**
     * Timestamp when the chat room was created.
     * Set automatically in the constructor.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the most recent message in this chat room.
     * Used for sorting chat rooms by activity.
     * Null if no messages have been sent yet.
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * Default constructor that initializes the createdAt timestamp.
     */
    public ChatRoom(){
        this.createdAt = LocalDateTime.now();
    }
}
