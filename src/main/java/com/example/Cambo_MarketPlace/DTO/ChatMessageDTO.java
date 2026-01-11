package com.example.Cambo_MarketPlace.DTO;

import java.time.LocalDateTime;

public class ChatMessageDTO {
    private Long chatRoomId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime sentAt;
    private MessageType type;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    public ChatMessageDTO() {
        this.sentAt = LocalDateTime.now();
    }

    public ChatMessageDTO(Long chatRoomId, Long senderId, String senderUsername, String content, MessageType type) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.content = content;
        this.type = type;
        this.sentAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
