package com.pond.server.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pond.server.model.Message;

/**
 * Repository interface for {@link Message} entity database operations.
 * 
 * <p>Manages chat messages within chat rooms, including message retrieval,
 * read status tracking, and unread count aggregation. Provides both paginated
 * and unpaginated query methods for flexible message loading strategies.</p>
 * 
 * @author Pond Team
 * @see Message
 * @see ChatRoom
 */
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Gets all messages in a chat room, ordered chronologically (oldest first).
     * Used for displaying full chat history.
     * 
     * @param roomId the chat room identifier
     * @return list of messages ordered by timestamp ascending
     */
    List<Message> findByRoomIdOrderByTimestampAsc(String roomId);

    /**
     * Gets messages in a chat room with pagination, ordered chronologically.
     * Used for loading message history in chunks (infinite scroll).
     * 
     * @param roomId the chat room identifier
     * @param pageable pagination parameters
     * @return page of messages ordered by timestamp ascending
     */
    List<Message> findByRoomIdOrderByTimestampAsc(String roomId, Pageable pageable);

    /**
     * Gets most recent messages in a chat room with pagination.
     * Used for fetching the last message to display in chat room list.
     * 
     * @param roomId the chat room identifier
     * @param pageable pagination parameters (typically limit of 1)
     * @return page of messages ordered by timestamp descending
     */
    List<Message> findByRoomIdOrderByTimestampDesc(String roomId, Pageable pageable);

    /**
     * Marks all unread messages as read for a specific user in a chat room.
     * Used when a user opens a chat to mark the other person's messages as read.
     * 
     * @param roomId the chat room identifier
     * @param userGU UUID of the user reading the messages
     * @return number of messages marked as read
     */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.roomId = :roomId AND m.senderGU != :userGU AND m.isRead = false")
    int markRoomMessagesAsRead(@Param("roomId") String roomId, @Param("userGU") UUID userGU);

    /**
     * Counts unread messages for a user in a specific chat room.
     * Used for showing unread badge count on individual chat rooms.
     * 
     * @param roomId the chat room identifier
     * @param userGU UUID of the user
     * @return count of unread messages
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.roomId = :roomId AND m.senderGU != :userGU AND m.isRead = false")
    long countUnreadMessages(@Param("roomId") String roomId, @Param("userGU") UUID userGU);

    /**
     * Counts total unread messages across all chat rooms for a user.
     * Used for showing global unread notification count in the header.
     * 
     * @param userGU UUID of the user
     * @return total count of unread messages across all rooms
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.senderGU != :userGU AND m.isRead = false AND m.roomId IN (SELECT cr.roomId FROM ChatRoom cr WHERE cr.sellerGU = :userGU OR cr.buyerGU = :userGU)")
    long countUnreadMessagesByUser(@Param("userGU") UUID userGU);
}