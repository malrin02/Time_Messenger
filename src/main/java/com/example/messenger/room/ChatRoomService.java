package com.example.messenger.room;

import com.example.messenger.user.User;
import com.example.messenger.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.messenger.redis.MessageRedisService;
import com.example.messenger.websocket.ChatMessage;
import com.example.messenger.websocket.RoomUpdatedMessage;
import com.example.messenger.websocket.RoomTimerUpdatedMessage;
import com.example.messenger.websocket.WebSocketSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;
    private final MessageRedisService messageRedisService;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatRoomResponse createRoom(ChatRoomCreateRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String inviteCode = generateInviteCode();

        ChatRoom room = new ChatRoom(
                request.getTitle(),
                owner,
                request.getDurationMinutes(),
                inviteCode
        );

        ChatRoom savedRoom = chatRoomRepository.save(room);

        ChatRoomMember ownerMember = new ChatRoomMember(savedRoom, owner);
        chatRoomMemberRepository.save(ownerMember);

        return new ChatRoomResponse(savedRoom);
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String code;

        do {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < 8; i++) {
                int index = (int) (Math.random() * chars.length());
                sb.append(chars.charAt(index));
            }

            code = sb.toString();
        } while (chatRoomRepository.existsByInviteCode(code));

        return code;
    }

    @Transactional
    public ChatRoomResponse joinRoom(Long roomId, ChatRoomJoinRequest request) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        if (room.isExpired()) {
            room.expire();
            throw new IllegalArgumentException("이미 만료된 채팅방입니다.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, request.getUserId())) {
            ChatRoomMember member = new ChatRoomMember(room, user);
            chatRoomMemberRepository.save(member);
        }

        return new ChatRoomResponse(room);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms() {
        return chatRoomRepository.findAllByOrderByIdDesc()
                .stream()
                .map(ChatRoomResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        return new ChatRoomResponse(room);
    }

    @Transactional
    public ChatRoomResponse updateRoomTimer(Long roomId, int durationMinutes) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        if (room.isExpired()) {
            room.expire();
            throw new IllegalArgumentException("이미 만료된 채팅방입니다.");
        }

        room.updateTimer(durationMinutes);
        messageRedisService.updateMessagesExpiration(room.getId(), room.getExpiresAt());

        RoomTimerUpdatedMessage timerUpdatedMessage = new RoomTimerUpdatedMessage(
                "ROOM_TIMER_UPDATED",
                room.getId(),
                durationMinutes,
                room.getExpiresAt()
        );

        try {
            String json = objectMapper.writeValueAsString(timerUpdatedMessage);
            sessionManager.broadcast(room.getId(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("타이머 변경 메시지 JSON 변환 실패", e);
        }

        return new ChatRoomResponse(room);
    }

    @Transactional
    public ChatRoomResponse updateRoomTitle(Long roomId, String title) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        if (room.isExpired()) {
            room.expire();
            throw new IllegalArgumentException("이미 만료된 채팅방입니다.");
        }

        room.updateTitle(title.trim());
        broadcastRoomUpdated(room);

        return new ChatRoomResponse(room);
    }

    @Transactional
    public ChatRoomResponse updateRoomImage(Long roomId, String imageUrl) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        if (room.isExpired()) {
            room.expire();
            throw new IllegalArgumentException("이미 만료된 채팅방입니다.");
        }

        room.updateImageUrl(imageUrl);
        broadcastRoomUpdated(room);

        return new ChatRoomResponse(room);
    }

    private void broadcastRoomUpdated(ChatRoom room) {
        RoomUpdatedMessage roomUpdatedMessage = new RoomUpdatedMessage(
                "ROOM_UPDATED",
                room.getId(),
                room.getTitle(),
                room.getImageUrl()
        );

        try {
            String json = objectMapper.writeValueAsString(roomUpdatedMessage);
            sessionManager.broadcast(room.getId(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("방 변경 메시지 JSON 변환 실패", e);
        }
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getRoomMessages(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new IllegalArgumentException("채팅방 멤버가 아닙니다.");
        }

        if (room.isExpired()) {
            return List.of();
        }

        return messageRedisService.getMessages(roomId);
    }

    @Transactional
    public ChatRoomResponse joinRoomByCode(ChatRoomJoinByCodeRequest request) {
        ChatRoom room = chatRoomRepository.findByInviteCode(request.getInviteCode().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 보안 코드입니다."));

        if (room.isExpired()) {
            room.expire();
            throw new IllegalArgumentException("이미 만료된 채팅방입니다.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(room.getId(), request.getUserId())) {
            ChatRoomMember member = new ChatRoomMember(room, user);
            chatRoomMemberRepository.save(member);
        }

        return new ChatRoomResponse(room);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyRooms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return chatRoomMemberRepository.findByUserIdOrderByJoinedAtDesc(user.getId())
                .stream()
                .map(member -> new ChatRoomResponse(member.getRoom()))
                .toList();
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        ChatRoomMember member = chatRoomMemberRepository.findByRoomIdAndUserId(room.getId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("참가하지 않은 채팅방입니다."));

        chatRoomMemberRepository.delete(member);
    }

    @Transactional(readOnly = true)
    public ChatRoomEnterResponse enterRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!chatRoomMemberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new IllegalArgumentException("채팅방 멤버가 아닙니다.");
        }

        if (room.isExpired()) {
            return new ChatRoomEnterResponse(
                    new ChatRoomResponse(room),
                    List.of()
            );
        }

        List<ChatMessage> messages = messageRedisService.getMessages(roomId);

        return new ChatRoomEnterResponse(
                new ChatRoomResponse(room),
                messages
        );
    }
}
