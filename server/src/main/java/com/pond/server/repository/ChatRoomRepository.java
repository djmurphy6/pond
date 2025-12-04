package com.pond.server.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pond.server.model.ChatRoom;

/**
 * Repository interface for {@link ChatRoom} entity database operations.
 * 
 * <p>Manages chat rooms created between buyers and sellers for specific listings.
 * Each chat room is uniquely identified by its roomId and contains a conversation
 * history accessible through the {@link MessageRepository}.</p>
 * 
 * @author Pond Team
 * @see ChatRoom
 * @see MessageRepository
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    
    /**
     * Finds a chat room by its application-level room ID.
     * Room IDs follow format: listing_{listingGU}_buyer_{buyerGU}
     * 
     * @param roomId the room identifier string
     * @return an Optional containing the chat room if found
     */
    Optional<ChatRoom> findByRoomId(String roomId);

    /**
     * Finds all chat rooms where a user is either the seller or buyer.
     * Results are ordered by most recent activity (lastMessageAt) descending,
     * with rooms without messages sorted by creation date.
     * 
     * @param userGU UUID of the user (matched against both seller and buyer)
     * @param userGU UUID of the user (duplicate param for OR clause)
     * @return list of chat rooms sorted by recent activity
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.sellerGU = :userGU OR cr.buyerGU = :userGU ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC")
    List<ChatRoom> findBySellerGUOrBuyersGU(@Param("userGU") UUID user1GU, @Param("userGU") UUID user2GU);

    /**
     * Finds all chat rooms associated with a specific listing.
     * A listing can have multiple chat rooms (one per interested buyer).
     * 
     * @param listingGU UUID of the listing
     * @return list of chat rooms for the listing
     */
    List<ChatRoom> findByListingGU(UUID listingGU);
}

