package com.pond.server.repository;

import com.pond.server.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    Optional<ChatRoom> findByRoomId(String roomId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.sellerGU = :userGU OR cr.buyerGU = :userGU ORDER BY cr.lastMessageAt DESC NULLS LAST, cr.createdAt DESC")
    List<ChatRoom> findBySellerGUOrBuyersGU(@Param("userGU") UUID user1GU, @Param("userGU") UUID user2GU);

    List<ChatRoom> findByListingGU(UUID listingGU);
}

