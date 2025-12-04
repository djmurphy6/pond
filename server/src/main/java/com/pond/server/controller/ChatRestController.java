
package com.pond.server.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pond.server.dto.ChatRoomDetailDTO;
import com.pond.server.dto.ChatRoomListDTO;
import com.pond.server.dto.MessageResponseDTO;
import com.pond.server.model.ChatRoom;
import com.pond.server.model.User;
import com.pond.server.service.ChatRoomService;
import com.pond.server.service.MessageService;

/**
 * REST controller for chat room and message operations.
 * Provides HTTP endpoints for chat room management, message retrieval, and read status tracking.
 * Works in conjunction with MessageController (WebSocket) for real-time messaging.
 */
@RestController
@RequestMapping("/chat")
public class ChatRestController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;

    /**
     * Constructs a new ChatRestController with required dependencies.
     *
     * @param chatRoomService the service for chat room operations
     * @param messageService the service for message operations
     */
    public ChatRestController(ChatRoomService chatRoomService, MessageService messageService) {
        this.chatRoomService = chatRoomService;
        this.messageService = messageService;
    }

    /**
     * Initializes or retrieves an existing chat room for a listing.
     * Creates a new chat room if one doesn't exist between the buyer and seller.
     *
     * @param listingGU the UUID of the listing
     * @param buyerGU the UUID of the buyer
     * @return ResponseEntity with chat room details
     */
    @PostMapping("/rooms/init")
    public ResponseEntity<?> initializeChatRoom(
            @RequestParam UUID listingGU,
            @RequestParam UUID buyerGU) {

        ChatRoom room = chatRoomService.getOrCreateChatRoom(listingGU, buyerGU);
        ChatRoomDetailDTO dto = chatRoomService.getChatRoomWithDetails(room.getRoomId(), buyerGU);
        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieves all chat rooms for the authenticated user.
     * Returns a summary list including last message and unread counts.
     *
     * @return ResponseEntity with list of chat room summaries or 401 if unauthorized
     */
    @GetMapping("/rooms")
    public ResponseEntity<?> getUserChatRooms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        List<ChatRoomListDTO> rooms = chatRoomService.getRoomListItems(currentUser.getUserGU());
        return ResponseEntity.ok(rooms);
    }

    /**
     * Retrieves detailed information about a specific chat room.
     * Includes listing details, other user info, and conversation metadata.
     *
     * @param roomId the ID of the chat room
     * @return ResponseEntity with chat room details or 401 if unauthorized
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<?> getChatRoomDetails(@PathVariable String roomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        ChatRoomDetailDTO dto = chatRoomService.getChatRoomWithDetails(roomId, currentUser.getUserGU());
        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieves messages for a chat room with pagination.
     * Verifies user has access to the chat room before retrieving messages.
     *
     * @param roomId the ID of the chat room
     * @param page the page number (default 0)
     * @param size the page size (default 50)
     * @return ResponseEntity with paginated messages or 401 if unauthorized
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> getRoomMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        // Verify user has access to this chat room
        chatRoomService.verifyChatRoomAccess(roomId, currentUser.getUserGU());

        List<MessageResponseDTO> messages = messageService.getRoomMessagesPaginated(roomId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Marks all unread messages in a chat room as read for the authenticated user.
     * Verifies user has access to the chat room before marking messages.
     *
     * @param roomId the ID of the chat room
     * @return ResponseEntity with success message or 401 if unauthorized
     */
    @PostMapping("/rooms/{roomId}/mark-read")
    public ResponseEntity<?> markMessagesAsRead(@PathVariable String roomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        chatRoomService.verifyChatRoomAccess(roomId, currentUser.getUserGU());
        messageService.markRoomMessagesAsRead(roomId, currentUser.getUserGU());

        return ResponseEntity.ok(Map.of("result", "Success"));
    }

    /**
     * Retrieves the total unread message count across all chat rooms for the authenticated user.
     * Used for notification badges.
     *
     * @return ResponseEntity with unread count or 401 if unauthorized
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadMessageCount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        long unreadCount = messageService.getTotalUnreadCount(currentUser.getUserGU());
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }
}