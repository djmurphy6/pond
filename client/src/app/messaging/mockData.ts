import { ChatRoomListDTO, MessageResponseDTO } from "@/api/WebTypes";

// Mock chat rooms
export const mockChatRooms: ChatRoomListDTO[] = [
    {
        roomId: "room-1",
        listingGU: "listing-1",
        listingTitle: "MacBook Pro 2021 16-inch",
        listingImage: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400",
        otherUserGU: "user-1",
        otherUsername: "john_doe",
        otherUserAvatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=john",
        lastMessage: "Is it still available?",
        lastMessageAt: new Date(Date.now() - 1000 * 60 * 5).toISOString(), // 5 minutes ago
        unreadCount: 2,
    },
    {
        roomId: "room-2",
        listingGU: "listing-2",
        listingTitle: "Calculus Textbook - 8th Edition",
        listingImage: "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400",
        otherUserGU: "user-2",
        otherUsername: "sarah_smith",
        otherUserAvatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=sarah",
        lastMessage: "Great! When can we meet?",
        lastMessageAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(), // 30 minutes ago
        unreadCount: 0,
    },
    {
        roomId: "room-3",
        listingGU: "listing-3",
        listingTitle: "Dorm Mini Fridge",
        listingImage: "https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5?w=400",
        otherUserGU: "user-3",
        otherUsername: "mike_wilson",
        otherUserAvatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=mike",
        lastMessage: "Thanks for your interest!",
        lastMessageAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(), // 2 hours ago
        unreadCount: 1,
    },
    {
        roomId: "room-4",
        listingGU: "listing-4",
        listingTitle: "Nike Running Shoes Size 10",
        listingImage: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400",
        otherUserGU: "user-4",
        otherUsername: "emma_jones",
        otherUserAvatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=emma",
        lastMessage: "They look great in the photos!",
        lastMessageAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(), // 1 day ago
        unreadCount: 0,
    },
    {
        roomId: "room-5",
        listingGU: "listing-5",
        listingTitle: "Desk Lamp - Adjustable LED",
        listingImage: "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=400",
        otherUserGU: "user-5",
        otherUsername: "alex_brown",
        otherUserAvatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=alex",
        lastMessage: "Can you do $20?",
        lastMessageAt: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(), // 2 days ago
        unreadCount: 0,
    },
];

// Mock messages for each room
export const mockMessages: Record<string, MessageResponseDTO[]> = {
    "room-1": [
        {
            id: "msg-1",
            roomId: "room-1",
            senderGU: "user-1",
            content: "Hi! I'm interested in the MacBook. Is it still available?",
            timestamp: new Date(Date.now() - 1000 * 60 * 60).toISOString(), // 1 hour ago
            isRead: true,
        },
        {
            id: "msg-2",
            roomId: "room-1",
            senderGU: "current-user",
            content: "Yes, it's still available! It's in excellent condition.",
            timestamp: new Date(Date.now() - 1000 * 60 * 55).toISOString(),
            isRead: true,
        },
        {
            id: "msg-3",
            roomId: "room-1",
            senderGU: "user-1",
            content: "Great! Does it come with the charger and original box?",
            timestamp: new Date(Date.now() - 1000 * 60 * 50).toISOString(),
            isRead: true,
        },
        {
            id: "msg-4",
            roomId: "room-1",
            senderGU: "current-user",
            content: "Yes! It comes with the original charger, box, and all accessories. I also have AppleCare until next year.",
            timestamp: new Date(Date.now() - 1000 * 60 * 45).toISOString(),
            isRead: true,
        },
        {
            id: "msg-5",
            roomId: "room-1",
            senderGU: "user-1",
            content: "Perfect! Can we meet on campus tomorrow?",
            timestamp: new Date(Date.now() - 1000 * 60 * 10).toISOString(),
            isRead: false,
        },
        {
            id: "msg-6",
            roomId: "room-1",
            senderGU: "user-1",
            content: "Is it still available?",
            timestamp: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
            isRead: false,
        },
    ],
    "room-2": [
        {
            id: "msg-7",
            roomId: "room-2",
            senderGU: "user-2",
            content: "Hi! Is the Calculus textbook still available?",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 3).toISOString(),
            isRead: true,
        },
        {
            id: "msg-8",
            roomId: "room-2",
            senderGU: "current-user",
            content: "Yes it is! It's barely used, only a few notes in pencil.",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2.5).toISOString(),
            isRead: true,
        },
        {
            id: "msg-9",
            roomId: "room-2",
            senderGU: "user-2",
            content: "Perfect! Would you take $40 for it?",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
            isRead: true,
        },
        {
            id: "msg-10",
            roomId: "room-2",
            senderGU: "current-user",
            content: "Sure, that works for me!",
            timestamp: new Date(Date.now() - 1000 * 60 * 60).toISOString(),
            isRead: true,
        },
        {
            id: "msg-11",
            roomId: "room-2",
            senderGU: "user-2",
            content: "Great! When can we meet?",
            timestamp: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
            isRead: true,
        },
    ],
    "room-3": [
        {
            id: "msg-12",
            roomId: "room-3",
            senderGU: "user-3",
            content: "I'm interested in the mini fridge. How old is it?",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString(),
            isRead: true,
        },
        {
            id: "msg-13",
            roomId: "room-3",
            senderGU: "current-user",
            content: "Thanks for your interest! It's about 1 year old and works perfectly.",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
            isRead: false,
        },
    ],
    "room-4": [
        {
            id: "msg-14",
            roomId: "room-4",
            senderGU: "user-4",
            content: "Are these shoes true to size?",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 25).toISOString(),
            isRead: true,
        },
        {
            id: "msg-15",
            roomId: "room-4",
            senderGU: "current-user",
            content: "Yes! They fit like a standard Nike size 10.",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24.5).toISOString(),
            isRead: true,
        },
        {
            id: "msg-16",
            roomId: "room-4",
            senderGU: "user-4",
            content: "They look great in the photos!",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
            isRead: true,
        },
    ],
    "room-5": [
        {
            id: "msg-17",
            roomId: "room-5",
            senderGU: "user-5",
            content: "Is the lamp price negotiable?",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 50).toISOString(),
            isRead: true,
        },
        {
            id: "msg-18",
            roomId: "room-5",
            senderGU: "current-user",
            content: "I'm asking $25, but I'm open to reasonable offers.",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 49).toISOString(),
            isRead: true,
        },
        {
            id: "msg-19",
            roomId: "room-5",
            senderGU: "user-5",
            content: "Can you do $20?",
            timestamp: new Date(Date.now() - 1000 * 60 * 60 * 48).toISOString(),
            isRead: true,
        },
    ],
};

// Mock current user ID
export const MOCK_CURRENT_USER_GU = "current-user";

