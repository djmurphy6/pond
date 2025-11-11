
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

@RestController
@RequestMapping("/chat")
public class ChatRestController {

    private final ChatRoomService chatRoomService;
    private final MessageService messageService;

    public ChatRestController(ChatRoomService chatRoomService, MessageService messageService) {
        this.chatRoomService = chatRoomService;
        this.messageService = messageService;
    }

    // Initialize or get chat room for a listing
    @PostMapping("/rooms/init")
    public ResponseEntity<?> initializeChatRoom(
            @RequestParam UUID listingGU,
            @RequestParam UUID buyerGU) {

        ChatRoom room = chatRoomService.getOrCreateChatRoom(listingGU, buyerGU);
        ChatRoomDetailDTO dto = chatRoomService.getChatRoomWithDetails(room.getRoomId(), buyerGU);
        return ResponseEntity.ok(dto);
    }

    // Get all chat rooms for the authenticated user
    @GetMapping("/rooms")
    public ResponseEntity<?> getUserChatRooms() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        List<ChatRoomListDTO> rooms = chatRoomService.getRoomListItems(currentUser.getUserGU());
        return ResponseEntity.ok(rooms);
    }

    // Get specific chat room details
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<?> getChatRoomDetails(@PathVariable String roomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        ChatRoomDetailDTO dto = chatRoomService.getChatRoomWithDetails(roomId, currentUser.getUserGU());
        return ResponseEntity.ok(dto);
    }

    // Get messages for a chat room with pagination
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

    // Mark messages as read
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

    // Get total unread message count for the authenticated user
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