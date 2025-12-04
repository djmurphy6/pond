package com.pond.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a chat message in the Pond marketplace.
 * 
 * <p>Messages are sent between buyers and sellers within a {@link ChatRoom}.
 * The system tracks read status to provide unread message counts and
 * notification badges.</p>
 * 
 * <p>Messages are delivered via WebSocket for real-time communication
 * and persisted to the database for message history.</p>
 * 
 * @author Pond Team
 * @see ChatRoom
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /**
     * Unique identifier for the message (UUID).
     * Generated automatically by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * ID of the chat room this message belongs to.
     * References {@link ChatRoom#roomId}.
     */
    @Column(name = "room_id", nullable = false)
    private String roomId;

    /**
     * UUID of the user who sent this message.
     */
    @Column(name = "sender_gu", nullable = false)
    private UUID senderGU;

    /**
     * Text content of the message.
     * Stored as TEXT to allow for long messages.
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Timestamp when the message was sent.
     * Set automatically in the constructor.
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Flag indicating whether the message has been read by the recipient.
     * Defaults to false for new messages.
     */
    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    /**
     * Constructs a new Message with the specified parameters.
     * Sets timestamp to current time and isRead to false.
     * 
     * @param roomId the chat room ID
     * @param senderGU the UUID of the sender
     * @param content the message content
     */
    public Message(String roomId, UUID senderGU, String content) {
        this.roomId = roomId;
        this.senderGU = senderGU;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    /**
     * Sets the read status of the message.
     * 
     * @param b true if the message has been read, false otherwise
     */
    public void setIsRead(boolean b) {
        this.isRead = b;
    }
}