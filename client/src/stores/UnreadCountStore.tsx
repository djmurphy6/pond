"use client";

import { useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client, IMessage } from "@stomp/stompjs";
import api from "@/api/WebService";
import { appConfig } from "@/api/WebService";
import { ErrorResponse } from "@/api/WebTypes";

export function useUnreadCount(userGU?: string, token?: string) {
  const clientRef = useRef<Client | null>(null);
  const [connected, setConnected] = useState(false);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [loading, setLoading] = useState(true);

  // Fetch initial unread count
  useEffect(() => {
    if (!userGU || !token) {
      setLoading(false);
      return;
    }

    async function fetchUnreadCount() {
      const response = await api.GetUnreadCount();
      if (!(response instanceof ErrorResponse)) {
        setUnreadCount(response.unreadCount);
      }
      setLoading(false);
    }

    fetchUnreadCount();
  }, [userGU, token]);

  // Set up WebSocket connection
  useEffect(() => {
    if (!userGU || !token) return;

    const socketUrl = process.env.NEXT_PUBLIC_WS_URL || `${appConfig.url}/ws`;
    const socket = new SockJS(socketUrl);

    const client = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 5000,
      connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
      onConnect: () => {
        console.log("✅ Connected to WebSocket for unread count updates");
        setConnected(true);

        // Subscribe to user-specific unread count updates
        // Spring WebSocket automatically routes /user/queue/... to the authenticated user
        const sub = client.subscribe(`/user/queue/unread-count`, (msg: IMessage) => {
          try {
            const body = JSON.parse(msg.body);
            console.log("📬 Received unread count update:", body);
            if (typeof body.unreadCount === "number") {
              setUnreadCount(body.unreadCount);
            }
          } catch (e) {
            console.error("Failed to parse unread count message:", e);
          }
        });

        return () => sub.unsubscribe();
      },
      onDisconnect: () => {
        console.log("⚠️ Disconnected from unread count WebSocket");
        setConnected(false);
      },
      onStompError: (frame) => {
        console.error("STOMP error (unread count):", frame.headers["message"]);
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [userGU, token]);

  // Function to refresh unread count manually
  const refreshUnreadCount = async () => {
    const response = await api.GetUnreadCount();
    if (!(response instanceof ErrorResponse)) {
      setUnreadCount(response.unreadCount);
    }
  };

  return { unreadCount, connected, loading, refreshUnreadCount };
}

