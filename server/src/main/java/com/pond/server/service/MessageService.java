package com.pond.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pond.server.dto.MessageResponseDTO;
import com.pond.server.model.Message;
import com.pond.server.repository.ChatRoomRepository;
import com.pond.server.repository.MessageRepository;

/**
 * Service class for managing chat messages.
 * Handles message storage, retrieval, read status tracking, and message counts.
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * Constructs a new MessageService with required dependencies.
     *
     * @param messageRepository the repository for message data access
     * @param chatRoomRepository the repository for chat room data access
     */
    public MessageService(MessageRepository messageRepository, ChatRoomRepository chatRoomRepository) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    /**
     * Saves a new message to the database.
     * Sets timestamp to current time if not provided.
     *
     * @param message the message to save
     * @return the saved message as a MessageResponseDTO
     */
    @Transactional
    public MessageResponseDTO saveMessage(Message message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        Message savedMessage = messageRepository.save(message);
        return convertToResponseDTO(savedMessage);
    }

    /**
     * Retrieves all messages in a chat room, ordered by timestamp.
     *
     * @param roomId the ID of the chat room
     * @return a list of all messages in the room
     * @throws RuntimeException if chat room not found
     */
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getRoomMessages(String roomId) {
        // Just verify room exists
        chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        return messageRepository.findByRoomIdOrderByTimestampAsc(roomId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves messages in a chat room with pagination.
     *
     * @param roomId the ID of the chat room
     * @param page the page number (zero-based)
     * @param size the page size
     * @return a page of messages from the room
     */
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getRoomMessagesPaginated(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return messageRepository.findByRoomIdOrderByTimestampAsc(roomId, pageable)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Marks all unread messages in a chat room as read for a specific user.
     * Verifies user has access to the chat room before marking messages.
     *
     * @param roomId the ID of the chat room
     * @param userGU the UUID of the user marking messages as read
     * @return the number of messages marked as read
     * @throws RuntimeException if user not authorized to access the chat room
     */
    @Transactional
    public int markRoomMessagesAsRead(String roomId, UUID userGU) {
        // Verify user has access to this chat room
        verifyChatRoomAccess(roomId, userGU);
        return messageRepository.markRoomMessagesAsRead(roomId, userGU);
    }

    /**
     * Gets the count of unread messages in a specific chat room for a user.
     *
     * @param roomId the ID of the chat room
     * @param userGU the UUID of the user
     * @return the number of unread messages
     */
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(String roomId, UUID userGU) {
        return messageRepository.countUnreadMessages(roomId, userGU);
    }

    /**
     * Gets the total count of unread messages across all chat rooms for a user.
     *
     * @param userGU the UUID of the user
     * @return the total number of unread messages
     */
    @Transactional(readOnly = true)
    public long getTotalUnreadCount(UUID userGU) {
        return messageRepository.countUnreadMessagesByUser(userGU);
    }

    /**
     * Verifies that a user has access to a specific chat room.
     * User must be either the seller or buyer in the chat room.
     *
     * @param roomId the ID of the chat room
     * @param userGU the UUID of the user to verify
     * @throws RuntimeException if chat room not found or user not authorized
     */
    private void verifyChatRoomAccess(String roomId, UUID userGU) {
        var room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        if (!room.getSellerGU().equals(userGU) && !room.getBuyerGU().equals(userGU)) {
            throw new RuntimeException("Not authorized to access this chat");
        }
    }

    /**
     * Converts a Message entity to a MessageResponseDTO.
     *
     * @param message the message entity to convert
     * @return the MessageResponseDTO representation
     */
    private MessageResponseDTO convertToResponseDTO(Message message) {
        return new MessageResponseDTO(
                message.getId(),
                message.getRoomId(),
                message.getSenderGU(),
                message.getContent(),
                message.getTimestamp(),
                message.isRead()
        );
    }

    /**
     * Retrieves the most recent message in a chat room.
     *
     * @param roomId the ID of the chat room
     * @return the last message, or null if room has no messages
     */
    @Transactional(readOnly = true)
    public MessageResponseDTO getLastMessage(String roomId) {
        return messageRepository.findByRoomIdOrderByTimestampAsc(
                        roomId,
                        PageRequest.of(0, 1)
                ).stream()
                .findFirst()
                .map(this::convertToResponseDTO)
                .orElse(null);
    }

    /**
     * Deletes a message from the database.
     *
     * @param messageId the UUID of the message to delete
     */
    @Transactional
    public void deleteMessage(UUID messageId) {
        messageRepository.deleteById(messageId);
    }
}