package com.pond.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pond.server.dto.ChatRoomDetailDTO;
import com.pond.server.dto.ChatRoomListDTO;
import com.pond.server.model.ChatRoom;
import com.pond.server.model.Listing;
import com.pond.server.model.Message;
import com.pond.server.model.User;
import com.pond.server.repository.ChatRoomRepository;
import com.pond.server.repository.ListingRepository;
import com.pond.server.repository.MessageRepository;
import com.pond.server.repository.UserRepository;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public ChatRoomService(
            ChatRoomRepository chatRoomRepository,
            ListingRepository listingRepository,
            UserRepository userRepository,
            MessageRepository messageRepository){
        this.chatRoomRepository = chatRoomRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ChatRoom getOrCreateChatRoom(UUID listingGU, UUID buyerGU){
        Listing listing = listingRepository.findById(listingGU)
                .orElseThrow(()->new RuntimeException("Listing not found"));

        UUID sellerGU = listing.getUserGU();

        if (sellerGU.equals(buyerGU)){
            throw new RuntimeException("Seller cannot message themselves");
        }
        // Create a unique room ID
        String roomId = generateRoomId(listingGU, buyerGU);

        // Check if the room exists and if it doesn't create it.
        return chatRoomRepository.findByRoomId(roomId)
                .orElseGet(()->{
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setRoomId(roomId);
                    newRoom.setListingGU(listingGU);
                    newRoom.setSellerGU(sellerGU);
                    newRoom.setBuyerGU(buyerGU);
                    return chatRoomRepository.save(newRoom);
                });
    }

    private String generateRoomId(UUID listingGU, UUID buyerGU){
        return String.format("listing_%s_buyer_%s", listingGU.toString(), buyerGU.toString());
    }

    private List<ChatRoom> getUserChatRooms(UUID userGU){
        return chatRoomRepository.findBySellerGUOrBuyersGU(userGU, userGU);
    }

    public ChatRoom getChatRoom(String roomId){
        return chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(()-> new RuntimeException("Chat room not found"));
    }

    public ChatRoomDetailDTO getChatRoomWithDetails(String roomId, UUID currentUserGU) {
        ChatRoom room = chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        // Verify user is part of this conversation
        if (!room.getSellerGU().equals(currentUserGU) && !room.getBuyerGU().equals(currentUserGU)) {
            throw new RuntimeException("Not authorized to access this chat");
        }

        // Get listing details
        Listing listing = listingRepository.findById(room.getListingGU())
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        // Get other user info
        UUID otherUserGU = room.getSellerGU().equals(currentUserGU) ?
                room.getBuyerGU() : room.getSellerGU();

        User otherUser = userRepository.findById(otherUserGU)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new ChatRoomDetailDTO(
                room.getRoomId(),
                listing.getListingGU(),
                listing.getTitle(),
                listing.getPrice(),
                listing.getPicture1_url(),
                otherUser.getUserGU(),
                otherUser.getUsername(),
                otherUser.getAvatar_url(),
                room.getCreatedAt(),
                room.getLastMessageAt()
        );
    }

    public List<ChatRoomListDTO> getRoomListItems(UUID currentUserGU){
        List<ChatRoom> rooms = getUserChatRooms(currentUserGU);

        return rooms.stream().map(room ->{
            Listing listing = listingRepository.findById(room.getListingGU())
                    .orElse(null);

            if (listing == null){
                return null;
            }

            UUID otherUserGU = room.getSellerGU().equals(currentUserGU) ? room.getBuyerGU() : room.getSellerGU();

            User otherUser = userRepository.findById(otherUserGU)
                    .orElse(null);
            if (otherUser == null){
                return null;
            }

            // Get the last message
            List<Message> messages = messageRepository.findByRoomIdOrderByTimestampDesc(
                    room.getRoomId(),
                    org.springframework.data.domain.PageRequest.of(0,1)
            );

            String lastMessage = messages.isEmpty() ? "No messages yet" : messages.get(0).getContent();

            // Get unread message count
            long unreadCount = messageRepository.countUnreadMessages(room.getRoomId(), currentUserGU);

            // Determine if current user is the seller
            boolean isSeller = room.getSellerGU().equals(currentUserGU);

            return new ChatRoomListDTO(
                    room.getRoomId(),
                    listing.getListingGU(),
                    listing.getTitle(),
                    listing.getPicture1_url(),
                    otherUser.getUserGU(),
                    otherUser.getUsername(),
                    otherUser.getAvatar_url(),
                    lastMessage,
                    room.getLastMessageAt(),
                    unreadCount,
                    isSeller
            );
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    @Transactional
    public void updateLastMessageTime(String roomId) {
        ChatRoom room = getChatRoom(roomId);
        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);
    }
    public void verifyChatRoomAccess(String roomId, UUID userGU) {
        ChatRoom room = getChatRoom(roomId);
        if (!room.getSellerGU().equals(userGU) && !room.getBuyerGU().equals(userGU)) {
            throw new RuntimeException("Not authorized to access this chat");
        }
    }
}
