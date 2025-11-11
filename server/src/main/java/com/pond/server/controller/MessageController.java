
package com.pond.server.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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

    @MessageMapping("/chat/send")
    public void sendMessage(@Payload MessageDTO messageDTO, Principal principal) {
        try {
            System.out.println("✅ 1. Message received from WebSocket");
            System.out.println("   Content: " + messageDTO.getContent());
            System.out.println("   RoomId: " + messageDTO.getRoomId());

            // Get the username from principal (Spring Security sets this to username)
            String senderIdentifier = principal.getName();
            System.out.println("✅ 2. Sender identifier: " + senderIdentifier);

            // Try to find by username first, then by email as fallback
            User sender = userRepository.findByUsername(senderIdentifier)
                    .or(() -> userRepository.findByEmail(senderIdentifier))
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            UUID senderGU = sender.getUserGU();
            System.out.println("✅ 3. Sender found - Email: " + sender.getEmail() + ", GU: " + senderGU);

            // Verify the user is part of the chat room
            ChatRoom room = chatRoomService.getChatRoom(messageDTO.getRoomId());
            System.out.println("✅ 4. Chat room found");

            if (!room.getSellerGU().equals(senderGU) && !room.getBuyerGU().equals(senderGU)) {
                throw new RuntimeException("Not authorized");
            }
            System.out.println("✅ 5. User authorized");

            // Create and save message
            Message message = new Message(messageDTO.getRoomId(), senderGU, messageDTO.getContent());
            System.out.println("✅ 6. Message object created");
            System.out.println("   Message ID (before save): " + message.getId());
            System.out.println("   Message timestamp: " + message.getTimestamp());
            System.out.println("   Message isRead: " + message.isRead());

            MessageResponseDTO savedMessage = messageService.saveMessage(message);
            System.out.println("✅ 7. Message saved to database!");
            System.out.println("   Saved message ID: " + savedMessage.getId());

            // Update last message time in chatroom
            chatRoomService.updateLastMessageTime(messageDTO.getRoomId());
            System.out.println("✅ 8. Chat room last_message_at updated");

            // Send message to all subscribers in the room
            messagingTemplate.convertAndSend(
                    "/topic/room/" + messageDTO.getRoomId(),
                    savedMessage
            );
            System.out.println("✅ 9. Message broadcasted to room subscribers");

            // Send unread count notification to the other user
            UUID recipientGU = room.getSellerGU().equals(senderGU) ?
                    room.getBuyerGU() : room.getSellerGU();

            // Get recipient's username for WebSocket routing (Spring uses username, not UUID)
            User recipient = userRepository.findById(recipientGU)
                    .orElseThrow(() -> new RuntimeException("Recipient not found"));

            long recipientUnreadCount = messageService.getTotalUnreadCount(recipientGU);
            messagingTemplate.convertAndSendToUser(
                    recipient.getUsername(),  // Use username instead of UUID
                    "/queue/unread-count",
                    java.util.Map.of("unreadCount", recipientUnreadCount)
            );
            System.out.println("✅ 10. Unread count notification sent to recipient: " + recipient.getUsername() + " (GU: " + recipientGU + ")");

        } catch (Exception e) {
            // Log error and send error notification
            System.err.println("❌ ERROR in sendMessage: " + e.getMessage());
            System.err.println("❌ Error class: " + e.getClass().getName());
            e.printStackTrace();

            try {
                messagingTemplate.convertAndSend(
                        "/topic/room/" + messageDTO.getRoomId(),
                        new NotificationDTO("Error: " + e.getMessage(), messageDTO.getRoomId())
                );
            } catch (Exception notifError) {
                System.err.println("❌ Failed to send error notification: " + notifError.getMessage());
            }
        }
    }
}