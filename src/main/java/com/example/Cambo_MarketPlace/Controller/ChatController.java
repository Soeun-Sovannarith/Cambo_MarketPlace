package com.example.Cambo_MarketPlace.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.Cambo_MarketPlace.DTO.ChatMessageDTO;
import com.example.Cambo_MarketPlace.Models.Message;
import com.example.Cambo_MarketPlace.Service.ChatService;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle sending messages
     * Client sends to: /app/chat.sendMessage
     * Message is broadcast to: /topic/chatroom/{chatRoomId}
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage) {
        // Save message to database
        Message savedMessage = chatService.saveMessage(chatMessage);
        
        // Convert to DTO
        ChatMessageDTO responseDTO = chatService.convertToDTO(savedMessage);
        
        // Send to specific chat room
        messagingTemplate.convertAndSend(
            "/topic/chatroom/" + chatMessage.getChatRoomId(), 
            responseDTO
        );
        
        // Get the chat room to find the other user
        var chatRoom = chatService.getChatRoom(chatMessage.getChatRoomId());
        if (chatRoom.isPresent()) {
            Long recipientId = chatMessage.getSenderId().equals(chatRoom.get().getBuyer().getId()) 
                ? chatRoom.get().getSeller().getId() 
                : chatRoom.get().getBuyer().getId();
            
            // Send notification to the other user
            messagingTemplate.convertAndSend(
                "/topic/user/" + recipientId + "/notifications",
                responseDTO
            );
        }
    }

    /**
     * Handle user joining a chat room
     * Client sends to: /app/chat.addUser
     * Message is broadcast to: /topic/chatroom/{chatRoomId}
     */
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessageDTO chatMessage, 
                       SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSenderUsername());
        headerAccessor.getSessionAttributes().put("chatRoomId", chatMessage.getChatRoomId());
        
        // Notify others in the chat room
        messagingTemplate.convertAndSend(
            "/topic/chatroom/" + chatMessage.getChatRoomId(),
            chatMessage
        );
    }

    /**
     * REST endpoint to get chat room messages
     */
    @GetMapping("/api/chatrooms/{chatRoomId}/messages")
    @ResponseBody
    public List<ChatMessageDTO> getChatRoomMessages(@PathVariable Long chatRoomId) {
        List<Message> messages = chatService.getChatRoomMessages(chatRoomId);
        return messages.stream()
                .map(chatService::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * REST endpoint to get user's chat rooms
     */
    @GetMapping("/api/users/{userId}/chatrooms")
    @ResponseBody
    public List<Long> getUserChatRooms(@PathVariable Long userId) {
        return chatService.getUserChatRooms(userId).stream()
                .map(chatRoom -> chatRoom.getId())
                .collect(Collectors.toList());
    }
}
