package com.pond.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pond.server.model.Message;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Get all messages in a room, ordered chronologically (oldest first)
    List<Message> findByRoomIdOrderByTimestampAsc(String roomId);

    // Get messages with pagination, most recent first
    // Used for: Loading message history in chunks
    List<Message> findByRoomIdOrderByTimestampAsc(String roomId, Pageable pageable);

    // Mark all unread messages as read for a specific user in a room
    // Used for: When user opens a chat, mark messages from other person as read
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.roomId = :roomId AND m.senderGU != :userGU AND m.isRead = false")
    int markRoomMessagesAsRead(@Param("roomId") String roomId, @Param("userGU") UUID userGU);

    // Count how many unread messages a user has in a room
    // Used for: Showing unread badge count in chat list
    @Query("SELECT COUNT(m) FROM Message m WHERE m.roomId = :roomId AND m.senderGU != :userGU AND m.isRead = false")
    long countUnreadMessages(@Param("roomId") String roomId, @Param("userGU") UUID userGU);

    // Count total unread messages across all rooms for a user
    // Used for: Showing total unread notification count
    @Query("SELECT COUNT(m) FROM Message m WHERE m.senderGU != :userGU AND m.isRead = false AND m.roomId IN (SELECT cr.roomId FROM ChatRoom cr WHERE cr.sellerGU = :userGU OR cr.buyerGU = :userGU)")
    long countUnreadMessagesByUser(@Param("userGU") UUID userGU);
}