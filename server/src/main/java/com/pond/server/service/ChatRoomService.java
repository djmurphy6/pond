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

/**
 * Service class for managing chat rooms.
 * Handles chat room creation, retrieval, access verification, and room list generation.
 */
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    /**
     * Constructs a new ChatRoomService with required dependencies.
     *
     * @param chatRoomRepository the repository for chat room data access
     * @param listingRepository the repository for listing data access
     * @param userRepository the repository for user data access
     * @param messageRepository the repository for message data access
     */
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

    /**
     * Gets an existing chat room or creates a new one for a listing and buyer.
     * Prevents sellers from messaging themselves.
     *
     * @param listingGU the UUID of the listing
     * @param buyerGU the UUID of the buyer (potential buyer)
     * @return the existing or newly created ChatRoom
     * @throws RuntimeException if listing not found or seller tries to message themselves
     */
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

    /**
     * Generates a unique room ID for a listing and buyer combination.
     * Format: "listing_{listingGU}_buyer_{buyerGU}"
     *
     * @param listingGU the UUID of the listing
     * @param buyerGU the UUID of the buyer
     * @return the generated room ID string
     */
    private String generateRoomId(UUID listingGU, UUID buyerGU){
        return String.format("listing_%s_buyer_%s", listingGU.toString(), buyerGU.toString());
    }

    /**
     * Retrieves all chat rooms where the user is either a seller or buyer.
     *
     * @param userGU the UUID of the user
     * @return a list of chat rooms involving the user
     */
    private List<ChatRoom> getUserChatRooms(UUID userGU){
        return chatRoomRepository.findBySellerGUOrBuyersGU(userGU, userGU);
    }

    /**
     * Retrieves a chat room by its room ID.
     *
     * @param roomId the ID of the chat room
     * @return the ChatRoom entity
     * @throws RuntimeException if chat room not found
     */
    @Transactional(readOnly = true)
    public ChatRoom getChatRoom(String roomId){
        return chatRoomRepository.findByRoomId(roomId)
                .orElseThrow(()-> new RuntimeException("Chat room not found"));
    }

    /**
     * Retrieves detailed information about a chat room.
     * Includes listing details, other user information, and seller/buyer role.
     * Verifies user is part of the conversation.
     *
     * @param roomId the ID of the chat room
     * @param currentUserGU the UUID of the current user
     * @return the detailed chat room information
     * @throws RuntimeException if chat room/listing/user not found or user not authorized
     */
    @Transactional(readOnly = true)
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

        // Determine if current user is the seller
        boolean isSeller = room.getSellerGU().equals(currentUserGU);

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
                room.getLastMessageAt(),
                listing.getSold(),
                isSeller
        );
    }

    /**
     * Retrieves a list of all chat rooms for a user with summary information.
     * Includes listing details, other user info, last message, and unread count.
     * Filters out rooms with missing listings or users.
     *
     * @param currentUserGU the UUID of the current user
     * @return a list of chat room summaries for display in the room list
     */
    @Transactional(readOnly = true)
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
                    isSeller,
                    listing.getSold()
            );
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    /**
     * Updates the last message timestamp for a chat room.
     * Called when a new message is sent to track conversation activity.
     *
     * @param roomId the ID of the chat room to update
     */
    @Transactional
    public void updateLastMessageTime(String roomId) {
        ChatRoom room = getChatRoom(roomId);
        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);
    }
    
    /**
     * Verifies that a user has access to a specific chat room.
     * User must be either the seller or buyer in the chat room.
     *
     * @param roomId the ID of the chat room
     * @param userGU the UUID of the user to verify
     * @throws RuntimeException if user not authorized to access the chat room
     */
    @Transactional(readOnly = true)
    public void verifyChatRoomAccess(String roomId, UUID userGU) {
        ChatRoom room = getChatRoom(roomId);
        if (!room.getSellerGU().equals(userGU) && !room.getBuyerGU().equals(userGU)) {
            throw new RuntimeException("Not authorized to access this chat");
        }
    }
}
