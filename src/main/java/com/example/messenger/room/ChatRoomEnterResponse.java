package com.example.messenger.room;

import com.example.messenger.websocket.ChatMessage;

import java.util.List;

public class ChatRoomEnterResponse {

    private final ChatRoomResponse room;
    private final List<ChatMessage> messages;

    public ChatRoomEnterResponse(ChatRoomResponse room, List<ChatMessage> messages) {
        this.room = room;
        this.messages = messages;
    }

    public ChatRoomResponse getRoom() {
        return room;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }
}