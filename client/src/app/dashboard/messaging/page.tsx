"use client";

//React
import { useEffect, useRef, useState } from "react";
import { useTheme } from "next-themes";

//Icons
import {
    Send,
    ImageIcon,
    ChevronLeft,
    MessageCircle,
    ArrowLeft,
    Menu,
} from "lucide-react";

// API
import api, { appConfig } from "@/api/WebService";

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
import Link from "next/link";
import { useChatSocket } from "@/stores/ChatSocket";
import { Sheet, SheetContent, SheetTitle } from "@/components/ui/sheet";
import { useSearchParams, useRouter } from "next/navigation";
import FullScreenSpinner from "@/components/FullScreenSpiner";
import { useUnreadCount } from "@/stores/UnreadCountStore";
import MobileHeader from "@/components/MobileHeader";
import { SideBarAside } from "@/components/SideBarAside";

export default function MessagingPage() {

    const router = useRouter();
    const searchParams = useSearchParams();
    const roomId = searchParams.get("roomId");
    const message = searchParams.get("message");

    const [chatRooms, setChatRooms] = useState<ChatRoom[]>([]);
    const [selectedRoomId, setSelectedRoomId] = useState<string | null>(roomId || null);
    const [messages, setMessages] = useState<Message[]>([]);
    const [mounted, setMounted] = useState(false);
    const [loading, setLoading] = useState(true);
    const [loadingMessages, setLoadingMessages] = useState(false);
    const { theme } = useTheme();
    const { userInfo } = useUserInfoStore();
    const [showSidebar, setShowSidebar] = useState(false);

    const messagesEndRef = useRef<HTMLDivElement | null>(null);
    const initialMessageSentRef = useRef<string | null>(null); // Track the message content that was sent

    const { connected, sendMessage } = useChatSocket(
        selectedRoomId,
        appConfig.access_token,
        (newMessage) => {
            setMessages((prev) => [...prev, newMessage]);
            if (selectedRoomId) {
                setChatRooms(prev => prev.map(room => {
                    if (room.roomId === selectedRoomId) {
                        return ({ ...room, lastMessage: newMessage.content, lastMessageAt: newMessage.timestamp });
                    } else {
                        return room;
                    }
                }))
            }
        }
    );

    // Listen to unread count updates
    const { refreshUnreadCount } = useUnreadCount(userInfo?.userGU, appConfig.access_token);

    useEffect(() => {
        setMounted(true);
    }, []);

    useEffect(() => {
        if (mounted) {
            GetChatRooms();
        }
    }, [mounted]);

    // Scroll to bottom whenever messages change
    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    // Handle initial message from URL params
    useEffect(() => {
        // Create a unique key for this message send attempt
        const messageKey = message && roomId ? `${roomId}:${message}` : null;

        if (
            message &&
            roomId &&
            roomId === selectedRoomId &&
            connected &&
            initialMessageSentRef.current !== messageKey
        ) {
            console.log("Sending initial message:", message);
            sendMessage(message);
            initialMessageSentRef.current = messageKey;
            // Clear the message from URL to prevent re-sending
            router.replace(`/dashboard/messaging?roomId=${roomId}`, { scroll: false });
        }
    }, [message, roomId, selectedRoomId, connected, router]);

    useEffect(() => {
        if (selectedRoomId) {
            (async () => {

                await GetMessages(selectedRoomId);
                // Mark messages as read when room is selected
                let res = await api.MarkMessagesAsRead({ roomID: selectedRoomId });
                if (res instanceof ErrorResponse) {
                    toast.error(res.body?.error);
                } else {
                    setChatRooms(prev => prev.map(room =>
                        room.roomId === selectedRoomId
                            ? { ...room, unreadCount: 0 }
                            : room
                    ));
                    refreshUnreadCount();
                }
            })();
        }
    }, [selectedRoomId]);

    async function GetChatRooms() {
        setLoading(true);
        const res = await api.GetChatRooms();
        setLoading(false);
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error);
        } else {
            setChatRooms(res);
            if (res.length > 0 && !selectedRoomId) {
                // Select first room by default (selling rooms first, then buying)
                const sellingRooms = res.filter(room => room.isSeller);
                const buyingRooms = res.filter(room => !room.isSeller);
                
                if (sellingRooms.length > 0) {
                    setSelectedRoomId(sellingRooms[0].roomId);
                } else if (buyingRooms.length > 0) {
                    setSelectedRoomId(buyingRooms[0].roomId);
                }
            }
        }
    }

    async function GetMessages(roomId: string) {
        setLoadingMessages(true);
        const res = await api.GetRoomMessages(roomId);
        setLoadingMessages(false);
        if (res instanceof ErrorResponse) {
            toast.error(res.body?.error);
        } else {
            setMessages(res);
        }
    }

    function scrollToBottom() {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }


    const selectedRoom = chatRooms.find(room => room.roomId === selectedRoomId);

    // Separate chat rooms by role
    const sellingRooms = chatRooms.filter(room => room.isSeller);
    const buyingRooms = chatRooms.filter(room => !room.isSeller);

    const SideBar = () => (
        <SideBarAside className="w-80">
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
                {/* <ThemeToggle /> */}
            </div>

            {/* Chat Rooms List */}
            <ScrollArea className="flex-1 overflow-y-auto">
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
                    <div className="space-y-4">
                        {/* Selling Section */}
                        {sellingRooms.length > 0 && (
                            <div>
                                <div className="px-3 pb-2">
                                    <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide">
                                        Selling ({sellingRooms.length})
                                    </h3>
                                </div>
                                <div className="p-2 space-y-1">
                                    {sellingRooms.map((room) => (
                                        <ChatRoomItem
                                            key={room.roomId}
                                            room={room}
                                            isSelected={room.roomId === selectedRoomId}
                                            onClick={() => setSelectedRoomId(room.roomId)}
                                        />
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* Buying Section */}
                        {buyingRooms.length > 0 && (
                            <div>
                                <div className="px-3 pb-2">
                                    <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide">
                                        Buying ({buyingRooms.length})
                                    </h3>
                                </div>
                                <div className="p-2 space-y-1">
                                    {buyingRooms.map((room) => (
                                        <ChatRoomItem
                                            key={room.roomId}
                                            room={room}
                                            isSelected={room.roomId === selectedRoomId}
                                            onClick={() => setSelectedRoomId(room.roomId)}
                                        />
                                    ))}
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </ScrollArea>
        </SideBarAside>
    )

    if (!mounted) return null;

    return (
        <div className="flex flex-col md:flex-row max-h-screen bg-background transition-colors duration-300 min-h-screen">
            {/* Sidebar */}
            <div className="hidden md:flex">
                <SideBar />
            </div>

            <Sheet open={showSidebar} onOpenChange={setShowSidebar}>
                <SheetTitle className="sr-only">Messages</SheetTitle>
                <SheetContent side="left" className="w-80">
                    <SideBar />
                </SheetContent>
            </Sheet>

            <MobileHeader onPress={setShowSidebar} />

            {/* Main Chat Area */}
            <main className="relative flex-1 flex flex-col min-h-0 transition-colors duration-300">
                {selectedRoom ? (
                    <>
                        {/* Chat Header */}
                        <div
                            className={`sticky top-0 z-10 p-4 border-b bg-muted/30 backdrop-blur-sm transition-colors duration-300 ${theme !== "dark" && "shadow-sm"
                                }`}
                        >
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

                        {/* Messages area */}
                        <div className="flex-1 min-h-0 overflow-y-auto px-4 py-2">
                            {(loadingMessages || !connected) ? (
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
                                <div className="space-y-4 pt-2">
                                    {messages.map((message) => (
                                        <MessageBubble
                                            key={message.id}
                                            message={message}
                                            isOwn={message.senderGU === userInfo?.userGU}
                                        />
                                    ))}
                                    <div ref={messagesEndRef} />
                                </div>
                            )}
                        </div>

                        {/* Message Input */}
                        <div
                            className={`sticky bottom-0 z-10 border-t transition-colors duration-300 ${theme !== "dark" && "shadow-[0_-2px_10px_rgba(0,0,0,0.05)]"
                                }`}
                        >
                            <MessageInput selectedRoomId={selectedRoomId} />
                        </div>
                    </>
                ) : loading ?
                    (
                        <FullScreenSpinner />
                    ) : (
                        <div className="flex items-center justify-center h-full text-muted-foreground">
                            <div className="text-center pt-[30vh] md:pt-[0vh]">
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

function MessageInput({ selectedRoomId }: { selectedRoomId: string | null }) {

    const { theme } = useTheme();

    const [messageInput, setMessageInput] = useState("");
    const [mounted, setMounted] = useState(false);

    const { sendMessage } = useChatSocket(
        selectedRoomId,
        appConfig.access_token,
    );

    useEffect(() => {
        setMounted(true);
    }, []);

    async function handleSendMessage() {
        if (!messageInput.trim() || !selectedRoomId) return;

        sendMessage(messageInput.trim());

        setMessageInput("");
    }

    if (!mounted) return null;
    return (
        <div className={`p-4 border-t bg-muted/40 transition-colors duration-300 ${theme !== "dark" && "shadow-[0_-2px_10px_rgba(0,0,0,0.05)]"}`}>
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
                    className="bg-uo-green text-white hover:bg-uo-green/90"
                >
                    <Send className="h-4 w-4" />
                </Button>
            </div>
        </div>
    )
}

function ChatRoomItem({ room, isSelected, onClick }: { room: ChatRoom; isSelected: boolean; onClick: () => void }) {
    const { theme } = useTheme();
    const [hasError, setHasError] = useState(false);

    return (
        <button
            onClick={onClick}
            className={`w-full p-3 rounded-lg text-left transition-all duration-200 cursor-pointer ${isSelected
                ? "bg-primary/10 border border-primary/20"
                : theme !== "dark" ? "hover:bg-background" : "hover:bg-muted/70"
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
                <div style={{ width: 185 }}>
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
                        : "bg-[#ffffff]"
                    }`}
            >
                <p className="text-sm whitespace-pre-wrap break-words">{message.content}</p>
                <p className={`text-xs mt-1 ${isOwn ? "text-white/70 text-right" : "text-muted-foreground"}`}>
                    {new Date(message.timestamp).toLocaleTimeString([], {
                        hour: "2-digit",
                        minute: "2-digit",
                    })}
                </p>
            </div>
        </div>
    );
}

