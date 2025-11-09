"use client";

import { useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client, IMessage } from "@stomp/stompjs";

//API
import type { Message } from "@/api/WebTypes";
import { appConfig } from "@/api/WebService";

export function useChatSocket(
  roomId: string | null,
  token?: string,
  onNewMessage?: (message: Message) => void
) {
  const clientRef = useRef<Client | null>(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (!roomId) return;

    const socketUrl = process.env.NEXT_PUBLIC_WS_URL || `${appConfig.url}/ws`;
    const socket = new SockJS(socketUrl);

    const client = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 5000,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      onConnect: () => {
        console.log("✅ Connected to WebSocket");
        setConnected(true);

        const sub = client.subscribe(`/topic/room/${roomId}`, (msg: IMessage) => {
          try {
            const body = JSON.parse(msg.body);
            onNewMessage?.(body);
          } catch (e) {
            console.error("Failed to parse incoming message:", e);
          }
        });

        return () => sub.unsubscribe();
      },
      onDisconnect: () => {
        console.log("⚠️ Disconnected from WebSocket");
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers["message"]);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [roomId, token]);

  function sendMessage(content: string) {
    if (!clientRef.current?.connected || !roomId) {
      console.warn("Socket not connected");
      return;
    }

    const payload = { roomId, content };
    clientRef.current.publish({
      destination: "/app/chat/send",
      body: JSON.stringify(payload),
    });
  }

  return { sendMessage, connected };
}
