package com.example.messenger.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public void addSession(Long roomId, WebSocketSession session) {
        roomSessions
                .computeIfAbsent(roomId, key -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void removeSession(WebSocketSession session) {
        for (Set<WebSocketSession> sessions : roomSessions.values()) {
            sessions.remove(session);
        }
    }

    public void broadcast(Long roomId, String message) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);

        if (sessions == null) {
            return;
        }

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    throw new RuntimeException("메시지 전송 실패", e);
                }
            }
        }
    }
}