package com.pond.server.service;

import com.pond.server.dto.MessageResponseDTO;
import com.pond.server.model.Message;
import com.pond.server.repository.MessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomService chatRoomService;

    public MessageService(MessageRepository messageRepository, ChatRoomService chatRoomService) {
        this.messageRepository = messageRepository;
        this.chatRoomService = chatRoomService;
    }

    /**
     * Save a new message to the database
     * @param message The message to save
     * @return MessageResponseDTO with the saved message details
     */
    @Transactional
    public MessageResponseDTO saveMessage(Message message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        // isRead is already false from constructor or entity default

        Message savedMessage = messageRepository.save(message);
        return convertToResponseDTO(savedMessage);
    }


    /**
     * Get all messages in a room ordered chronologically (oldest first)
     * @param roomId The room ID
     * @return List of MessageResponseDTOs
     */
    public List<MessageResponseDTO> getRoomMessages(String roomId) {
        chatRoomService.verifyChatRoomAccess(roomId, null); // Just verify room exists

        return messageRepository.findByRoomIdOrderByTimestampAsc(roomId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated messages from a room (most recent first)
     * @param roomId The room ID
     * @param page The page number (0-indexed)
     * @param size The page size
     * @return List of MessageResponseDTOs
     */
    public List<MessageResponseDTO> getRoomMessagesPaginated(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return messageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Mark all unread messages as read for a specific user in a room
     * @param roomId The room ID
     * @param userGU The user's UUID
     * @return Number of messages marked as read
     */
    @Transactional
    public int markRoomMessagesAsRead(String roomId, UUID userGU) {
        chatRoomService.verifyChatRoomAccess(roomId, userGU);
        return messageRepository.markRoomMessagesAsRead(roomId, userGU);
    }

    /**
     * Get the count of unread messages for a user in a specific room
     * @param roomId The room ID
     * @param userGU The user's UUID
     * @return Count of unread messages
     */
    public long getUnreadMessageCount(String roomId, UUID userGU) {
        return messageRepository.countUnreadMessages(roomId, userGU);
    }

    /**
     * Get the total count of unread messages for a user across all rooms
     * @param userGU The user's UUID
     * @return Total count of unread messages
     */
    public long getTotalUnreadCount(UUID userGU) {
        return messageRepository.countUnreadMessagesByUser(userGU);
    }

    /**
     * Convert Message entity to MessageResponseDTO
     * @param message The message entity
     * @return MessageResponseDTO
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
     * Get the last message in a room
     * @param roomId The room ID
     * @return MessageResponseDTO or null if no messages
     */
    public MessageResponseDTO getLastMessage(String roomId) {
        return messageRepository.findByRoomIdOrderByTimestampDesc(
                        roomId,
                        PageRequest.of(0, 1)
                ).stream()
                .findFirst()
                .map(this::convertToResponseDTO)
                .orElse(null);
    }

    /**
     * Delete a message (optional - for cleanup or user deletion)
     * @param messageId The message ID to delete
     */
    @Transactional
    public void deleteMessage(UUID messageId) {
        messageRepository.deleteById(messageId);
    }
}