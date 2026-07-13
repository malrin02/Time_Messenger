package com.example.messenger.websocket;

import com.example.messenger.redis.MessageRedisService;
import com.example.messenger.room.ChatRoom;
import com.example.messenger.room.ChatRoomMemberRepository;
import com.example.messenger.room.ChatRoomRepository;
import com.example.messenger.user.User;
import com.example.messenger.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRedisService messageRedisService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long roomId = getRoomId(session);
        Long userId = getUserId(session);

        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);

        if (room == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("존재하지 않는 채팅방입니다."));
            return;
        }

        if (room.isExpired()) {
            room.expire();
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("이미 만료된 채팅방입니다."));
            return;
        }

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("채팅방 멤버가 아닙니다."));
            return;
        }

        sessionManager.addSession(roomId, session);

        System.out.println("WebSocket connected - roomId=" + roomId + ", userId=" + userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long connectedRoomId = getRoomId(session);
        Long connectedUserId = getUserId(session);

        ChatMessageRequest request = objectMapper.readValue(
                message.getPayload(),
                ChatMessageRequest.class
        );

        if (!connectedRoomId.equals(request.getRoomId())) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("연결된 채팅방과 메시지 채팅방이 다릅니다."));
            return;
        }

        if (!connectedUserId.equals(request.getSenderId())) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("연결된 사용자와 메시지 발신자가 다릅니다."));
            return;
        }

        ChatRoom room = chatRoomRepository.findById(request.getRoomId()).orElse(null);

        if (room == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("존재하지 않는 채팅방입니다."));
            return;
        }

        if (room.isExpired()) {
            room.expire();

            RoomExpiredMessage expiredMessage = new RoomExpiredMessage(
                    "ROOM_EXPIRED",
                    request.getRoomId(),
                    "채팅방이 만료되었습니다."
            );

            String expiredJson = objectMapper.writeValueAsString(expiredMessage);
            sessionManager.broadcast(request.getRoomId(), expiredJson);

            session.close(CloseStatus.NORMAL.withReason("채팅방이 만료되었습니다."));
            return;
        }

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(
                request.getRoomId(),
                request.getSenderId()
        )) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("채팅방 멤버가 아닙니다."));
            return;
        }

        User sender = userRepository.findById(request.getSenderId())
                .orElse(null);

        String senderName = "알 수 없음";
        String senderProfileImageUrl = null;

        if (sender != null) {
            senderName = sender.getNickname() != null && !sender.getNickname().isBlank()
                    ? sender.getNickname()
                    : sender.getLoginId();
            senderProfileImageUrl = sender.getProfileImageUrl();
        }

        ChatMessage chatMessage = new ChatMessage(
                "MESSAGE",
                request.getRoomId(),
                request.getSenderId(),
                senderName,
                senderProfileImageUrl,
                request.getContent(),
                LocalDateTime.now()
        );

        messageRedisService.saveMessage(chatMessage, room.getExpiresAt());

        String responseMessage = objectMapper.writeValueAsString(chatMessage);

        sessionManager.broadcast(request.getRoomId(), responseMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.removeSession(session);

        System.out.println("WebSocket closed - code="
                + status.getCode()
                + ", reason="
                + status.getReason());
    }

    private Long getRoomId(WebSocketSession session) {
        URI uri = session.getUri();

        if (uri == null || uri.getQuery() == null) {
            throw new IllegalArgumentException("roomId가 필요합니다.");
        }

        String[] queryParams = uri.getQuery().split("&");

        for (String param : queryParams) {
            String[] keyValue = param.split("=");

            if (keyValue.length == 2 && keyValue[0].equals("roomId")) {
                return Long.parseLong(keyValue[1]);
            }
        }

        throw new IllegalArgumentException("roomId가 필요합니다.");
    }

    private Long getUserId(WebSocketSession session) {
        URI uri = session.getUri();

        if (uri == null || uri.getQuery() == null) {
            throw new IllegalArgumentException("userId가 필요합니다.");
        }

        String[] queryParams = uri.getQuery().split("&");

        for (String param : queryParams) {
            String[] keyValue = param.split("=");

            if (keyValue.length == 2 && keyValue[0].equals("userId")) {
                return Long.parseLong(keyValue[1]);
            }
        }

        throw new IllegalArgumentException("userId가 필요합니다.");
    }
}