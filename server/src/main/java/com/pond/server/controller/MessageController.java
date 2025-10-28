package com.pond.server.controller;

import com.pond.server.dto.MessageDTO;
import com.pond.server.dto.MessageResponseDTO;
import com.pond.server.dto.NotificationDTO;
import com.pond.server.model.ChatRoom;
import com.pond.server.model.Message;
import com.pond.server.model.User;
import com.pond.server.repository.ChatRoomRepository;
import com.pond.server.repository.ListingRepository;
import com.pond.server.repository.UserRepository;
import com.pond.server.service.ChatRoomService;
import com.pond.server.service.MessageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class MessageController {

    private final ChatRoomRepository chatRoomRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(
            ChatRoomRepository chatRoomRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            MessageService messageService,
            ChatRoomService chatRoomService,
            SimpMessagingTemplate messagingTemplate) {
        this.chatRoomRepository = chatRoomRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.chatRoomService = chatRoomService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageDTO messageDTO, Principal principal) {
        try {
            // Get the user's email from principal (authenticated via JWT)
            String senderEmail = principal.getName();
            User sender = userRepository.findByEmail(senderEmail)
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            UUID senderGU = sender.getUserGU();

            // Verify the user is part of the chat room
            ChatRoom room = chatRoomService.getChatRoom(messageDTO.getRoomId());
            if (!room.getSellerGU().equals(senderGU) && !room.getBuyerGU().equals(senderGU)) {
                throw new RuntimeException("Not authorized");
            }

            // Create and save message
            Message message = new Message(messageDTO.getRoomId(), senderGU, messageDTO.getContent());
            MessageResponseDTO savedMessage = messageService.saveMessage(message);

            // Update last message time in chat room
            chatRoomService.updateLastMessageTime(messageDTO.getRoomId());

            // Send message to all subscribers in the room
            messagingTemplate.convertAndSend(
                    "/topic/room/" + messageDTO.getRoomId(),
                    savedMessage
            );

            // Send notification to the other user
            UUID recipientGU = room.getSellerGU().equals(senderGU) ?
                    room.getBuyerGU() : room.getSellerGU();

            messagingTemplate.convertAndSendToUser(
                    recipientGU.toString(),
                    "/queue/notifications",
                    new NotificationDTO("New message in chat", messageDTO.getRoomId())
            );
        } catch (Exception e) {
            // Log error and send error notification
            messagingTemplate.convertAndSend(
                    "/topic/room/" + messageDTO.getRoomId(),
                    new NotificationDTO("Error: " + e.getMessage(), messageDTO.getRoomId())
            );
        }
    }
}