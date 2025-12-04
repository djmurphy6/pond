
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
import com.pond.server.service.ChatRoomService;
import com.pond.server.service.MessageService;
import com.pond.server.service.UserService;

/**
 * WebSocket controller for real-time messaging.
 * Handles message sending, broadcasting, and unread count notifications via STOMP/WebSocket.
 */
@Controller
public class MessageController {

    private final MessageService messageService;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Constructs a new MessageController with required dependencies.
     *
     * @param messageService the service for message operations
     * @param chatRoomService the service for chat room operations
     * @param userService the service for user operations
     * @param messagingTemplate the Spring STOMP messaging template for WebSocket communication
     */
    public MessageController(
            MessageService messageService,
            ChatRoomService chatRoomService,
            UserService userService,
            SimpMessagingTemplate messagingTemplate) {
        this.messageService = messageService;
        this.chatRoomService = chatRoomService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles incoming chat messages via WebSocket.
     * Verifies user authorization, saves the message, broadcasts to room subscribers,
     * and sends unread count notifications to the recipient.
     *
     * @param messageDTO the message DTO containing room ID and content
     * @param principal the authenticated user's principal
     */
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
            User sender = userService.findByUsernameOrEmail(senderIdentifier)
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
            User recipient = userService.findById(recipientGU)
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