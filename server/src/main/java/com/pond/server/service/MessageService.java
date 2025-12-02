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

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;

    public MessageService(MessageRepository messageRepository, ChatRoomRepository chatRoomRepository) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    @Transactional
    public MessageResponseDTO saveMessage(Message message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }

        Message savedMessage = messageRepository.save(message);
        return convertToResponseDTO(savedMessage);
    }

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


    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getRoomMessagesPaginated(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return messageRepository.findByRoomIdOrderByTimestampAsc(roomId, pageable)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public int markRoomMessagesAsRead(String roomId, UUID userGU) {
        // Verify user has access to this chat room
        verifyChatRoomAccess(roomId, userGU);
        return messageRepository.markRoomMessagesAsRead(roomId, userGU);
    }

    @Transactional(readOnly = true)
    public long getUnreadMessageCount(String roomId, UUID userGU) {
        return messageRepository.countUnreadMessages(roomId, userGU);
    }


    @Transactional(readOnly = true)
    public long getTotalUnreadCount(UUID userGU) {
        return messageRepository.countUnreadMessagesByUser(userGU);
    }


    private void verifyChatRoomAccess(String roomId, UUID userGU) {
        var room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        if (!room.getSellerGU().equals(userGU) && !room.getBuyerGU().equals(userGU)) {
            throw new RuntimeException("Not authorized to access this chat");
        }
    }


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


    @Transactional
    public void deleteMessage(UUID messageId) {
        messageRepository.deleteById(messageId);
    }
}