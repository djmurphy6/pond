"use client";

//React
import { useEffect, useState } from "react";
import { useTheme } from "next-themes";

//Icons
import {
    Send,
    ImageIcon,
    ChevronLeft,
    MessageCircle,
    ArrowLeft,
} from "lucide-react";

// API
import api from "@/api/WebService";

//ShadCN
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Separator } from "@/components/ui/separator";

//Internal
import ThemeToggle from "@/components/ThemeToggle";
import { ErrorResponse, ChatRoom, Message } from "@/api/WebTypes";
import { toast } from "sonner";
import { useUserInfoStore } from "@/stores/UserInfoStore";
import Image from "next/image";

// Mock data
import { mockChatRooms, mockMessages, MOCK_CURRENT_USER_GU } from "./mockData";
import Link from "next/link";

// Toggle this to switch between mock and real data
const USE_MOCK_DATA = true;

export default function MessagingPage() {
    const [chatRooms, setChatRooms] = useState<ChatRoom[]>([]);
    const [selectedRoomId, setSelectedRoomId] = useState<string | null>(null);
    const [messages, setMessages] = useState<Message[]>([]);
    const [messageInput, setMessageInput] = useState("");
    const [mounted, setMounted] = useState(false);
    const [loading, setLoading] = useState(true);
    const [loadingMessages, setLoadingMessages] = useState(false);
    const { theme } = useTheme();
    const { userInfo } = useUserInfoStore();

    useEffect(() => {
        setMounted(true);
    }, []);

    useEffect(() => {
        if (mounted) {
            GetChatRooms();
        }
    }, [mounted]);

    useEffect(() => {
        if (selectedRoomId) {
            GetMessages(selectedRoomId);
            // Mark messages as read when room is selected
            if (!USE_MOCK_DATA) {
                api.MarkMessagesAsRead(selectedRoomId);
            } else {
                // In mock mode, just clear unread count
                setChatRooms(prev => prev.map(room =>
                    room.roomId === selectedRoomId
                        ? { ...room, unreadCount: 0 }
                        : room
                ));
            }
        }
    }, [selectedRoomId]);

    async function GetChatRooms() {
        setLoading(true);

        if (USE_MOCK_DATA) {
            // Simulate API delay
            await new Promise(resolve => setTimeout(resolve, 500));
            // setChatRooms(mockChatRooms);
            // // Auto-select first room if available
            // if (mockChatRooms.length > 0 && !selectedRoomId) {
            //     setSelectedRoomId(mockChatRooms[0].roomId);
            // }
            setLoading(false);
            return;
        }

        const res = await api.GetChatRooms();
        setLoading(false);
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error);
        } else {
            setChatRooms(res);
            // Auto-select first room if available
            if (res.length > 0 && !selectedRoomId) {
                setSelectedRoomId(res[0].roomId);
            }
        }
    }

    async function GetMessages(roomId: string) {
        setLoadingMessages(true);

        if (USE_MOCK_DATA) {
            // Simulate API delay
            await new Promise(resolve => setTimeout(resolve, 300));
            // const roomMessages = mockMessages[roomId] || [];
            // setMessages(roomMessages);
            setLoadingMessages(false);
            return;
        }

        const res = await api.GetRoomMessages(roomId);
        setLoadingMessages(false);
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error);
        } else {
            setMessages(res);
        }
    }

    function handleSendMessage() {
        if (!messageInput.trim() || !selectedRoomId) return;

        if (USE_MOCK_DATA) {
            // Add message to mock data locally
            // const newMessage: Message = {
            //     id: `msg-${Date.now()}`,
            //     roomId: selectedRoomId,
            //     senderGU: MOCK_CURRENT_USER_GU,
            //     content: messageInput.trim(),
            //     timestamp: new Date().toISOString(),
            //     isRead: false,
            // };

            // setMessages(prev => [...prev, newMessage]);

            // Update the last message in the chat room list
            setChatRooms(prev => prev.map(room =>
                room.roomId === selectedRoomId
                    ? { ...room, lastMessage: messageInput.trim(), lastMessageAt: new Date().toISOString() }
                    : room
            ));

            setMessageInput("");
            toast.success("Message sent! (Mock mode)");
            return;
        }

        // TODO: Implement WebSocket message sending for real backend
        toast.info("WebSocket messaging not yet implemented");
        setMessageInput("");
    }

    const selectedRoom = chatRooms.find(room => room.roomId === selectedRoomId);

    if (!mounted) return null;

    return (
        <div className="flex h-screen bg-background transition-colors duration-300">
            {/* Sidebar */}
            <aside className={`w-80 border-r bg-muted/10 p-4 flex flex-col transition-colors duration-300 ${theme !== "dark" && "shadow-[2px_0_10px_rgba(0,0,0,0.15)]"}`}>
                {/* Header */}
                <Button variant={'link'} style={{ color: 'gray', justifyContent: 'flex-start' }} className="!p-0 !px-0 !py-0 hover:underline hover:bg-none cursor-pointer">
                    <Link style={{ flexDirection: 'row' }} className="flex items-center gap-1" href="/dashboard">
                        <ArrowLeft />
                        back to dashboard
                    </Link>
                </Button>

                {/* Top section */}
                <div className="flex items-center gap-2 mb-6 justify-between">
                    <h2 className="text-xl font-semibold">Messages</h2>
                    <ThemeToggle />
                </div>

                {/* Chat Rooms List */}
                <ScrollArea className="flex-1">
                    {loading ? (
                        <div className="p-2 space-y-2">
                            {Array.from({ length: 5 }).map((_, i) => (
                                <div key={i} className="p-3">
                                    <div className="flex gap-3">
                                        <Skeleton className="h-12 w-12 rounded-full" />
                                        <div className="flex-1 space-y-2">
                                            <Skeleton className="h-4 w-3/4" />
                                            <Skeleton className="h-3 w-1/2" />
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : chatRooms.length === 0 ? (
                        <div className="flex flex-col items-center justify-center h-full p-8 text-center text-muted-foreground">
                            <MessageCircle className="h-16 w-16 mb-4 opacity-50" />
                            <p className="text-lg font-medium mb-2">No messages yet</p>
                            <p className="text-sm">Start a conversation by messaging a seller</p>
                        </div>
                    ) : (
                        <div className="p-2 space-y-1">
                            {chatRooms.map((room) => (
                                <ChatRoomItem
                                    key={room.roomId}
                                    room={room}
                                    isSelected={room.roomId === selectedRoomId}
                                    onClick={() => setSelectedRoomId(room.roomId)}
                                />
                            ))}
                        </div>
                    )}
                </ScrollArea>
            </aside>

            {/* Main Chat Area */}
            <main className={`flex-1 flex flex-col transition-colors duration-300`}>
                {selectedRoom ? (
                    <>
                        {/* Chat Header */}
                        <div className={`p-4 border-b bg-muted/10 transition-colors duration-300 ${theme !== "dark" && "shadow-sm"}`}>
                            <div className="flex items-center gap-3">
                                <div className="relative h-10 w-10 rounded-full bg-muted overflow-hidden flex-shrink-0">
                                    {selectedRoom.listingImage ? (
                                        <Image
                                            src={selectedRoom.listingImage}
                                            alt={selectedRoom.listingTitle}
                                            fill
                                            className="object-cover"
                                        />
                                    ) : (
                                        <div className="w-full h-full flex items-center justify-center">
                                            <ImageIcon className="h-5 w-5 text-muted-foreground" />
                                        </div>
                                    )}
                                </div>
                                <div className="flex-1 min-w-0">
                                    <h3 className="font-semibold truncate">{selectedRoom.listingTitle}</h3>
                                    <p className="text-sm text-muted-foreground truncate">
                                        {selectedRoom.otherUsername}
                                    </p>
                                </div>
                            </div>
                        </div>

                        {/* Messages */}
                        <ScrollArea className="flex-1 p-4">
                            {loadingMessages ? (
                                <div className="space-y-4">
                                    {Array.from({ length: 5 }).map((_, i) => (
                                        <div key={i} className={`flex ${i % 2 === 0 ? 'justify-start' : 'justify-end'}`}>
                                            <Skeleton className="h-16 w-64 rounded-lg" />
                                        </div>
                                    ))}
                                </div>
                            ) : messages.length === 0 ? (
                                <div className="flex items-center justify-center h-full text-muted-foreground">
                                    <p>No messages yet. Start the conversation!</p>
                                </div>
                            ) : (
                                <div className="space-y-4">
                                    {messages.map((message) => (
                                        <MessageBubble
                                            key={message.id}
                                            message={message}
                                            isOwn={USE_MOCK_DATA ? message.senderGU === MOCK_CURRENT_USER_GU : message.senderGU === userInfo?.userGU}
                                        />
                                    ))}
                                </div>
                            )}
                        </ScrollArea>

                        {/* Message Input */}
                        <div className={`p-4 border-t bg-muted/10 transition-colors duration-300 ${theme !== "dark" && "shadow-[0_-2px_10px_rgba(0,0,0,0.05)]"}`}>
                            <div className="flex gap-2">
                                <Input
                                    placeholder="Type a message..."
                                    value={messageInput}
                                    onChange={(e) => setMessageInput(e.target.value)}
                                    onKeyDown={(e) => {
                                        if (e.key === "Enter" && !e.shiftKey) {
                                            e.preventDefault();
                                            handleSendMessage();
                                        }
                                    }}
                                    className="flex-1"
                                />
                                <Button
                                    onClick={handleSendMessage}
                                    disabled={!messageInput.trim()}
                                    size="icon"
                                >
                                    <Send className="h-4 w-4" />
                                </Button>
                            </div>
                        </div>
                    </>
                ) : (
                    <div className="flex items-center justify-center h-full text-muted-foreground">
                        <div className="text-center">
                            <MessageCircle className="h-24 w-24 mx-auto mb-4 opacity-50" />
                            <p className="text-xl font-medium">Select a conversation</p>
                            <p className="text-sm mt-2">Choose from your existing messages</p>
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
}

function ChatRoomItem({ room, isSelected, onClick }: { room: ChatRoom; isSelected: boolean; onClick: () => void }) {
    const { theme } = useTheme();
    const [hasError, setHasError] = useState(false);

    return (
        <button
            onClick={onClick}
            className={`w-full p-3 rounded-lg text-left transition-all duration-200 ${isSelected
                ? "bg-primary/10 border border-primary/20"
                : "hover:bg-muted/50"
                }`}
        >
            <div className="flex gap-3">
                <div className="relative h-12 w-12 rounded-full bg-muted overflow-hidden flex-shrink-0">
                    {room.listingImage && !hasError ? (
                        <Image
                            src={room.listingImage}
                            alt={room.listingTitle}
                            fill
                            className="object-cover"
                            onError={() => setHasError(true)}
                        />
                    ) : (
                        <div className="w-full h-full flex items-center justify-center">
                            <ImageIcon className="h-6 w-6 text-muted-foreground" />
                        </div>
                    )}
                </div>
                <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-1">
                        <h4 className="font-semibold text-sm truncate">{room.listingTitle}</h4>
                        {room.unreadCount > 0 && (
                            <span className="ml-2 px-2 py-0.5 text-xs bg-primary text-primary-foreground rounded-full">
                                {room.unreadCount}
                            </span>
                        )}
                    </div>
                    <p className="text-xs text-muted-foreground mb-1">{room.otherUsername}</p>
                    <p className="text-sm text-muted-foreground truncate">
                        {room.lastMessage || "No messages yet"}
                    </p>
                </div>
            </div>
        </button>
    );
}

function MessageBubble({ message, isOwn }: { message: Message; isOwn: boolean }) {
    const { theme } = useTheme();

    return (
        <div className={`flex ${isOwn ? "justify-end" : "justify-start"}`}>
            <div
                className={`max-w-[70%] rounded-lg p-3 ${isOwn
                    ? "bg-gradient-to-br from-green-700 to-emerald-600 text-white"
                    : theme === "dark"
                        ? "bg-muted"
                        : "bg-muted/50"
                    }`}
            >
                <p className="text-sm whitespace-pre-wrap break-words">{message.content}</p>
                <p className={`text-xs mt-1 ${isOwn ? "text-white/70" : "text-muted-foreground"}`}>
                    {new Date(message.timestamp).toLocaleTimeString([], {
                        hour: "2-digit",
                        minute: "2-digit",
                    })}
                </p>
            </div>
        </div>
    );
}

