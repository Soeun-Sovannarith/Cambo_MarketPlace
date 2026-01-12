package com.example.Cambo_MarketPlace.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Cambo_MarketPlace.DTO.ChatMessageDTO;
import com.example.Cambo_MarketPlace.Models.ChatRoom;
import com.example.Cambo_MarketPlace.Models.Message;
import com.example.Cambo_MarketPlace.Models.Product;
import com.example.Cambo_MarketPlace.Models.User;
import com.example.Cambo_MarketPlace.Repository.ChatRoomRepository;
import com.example.Cambo_MarketPlace.Repository.MessageRepository;
import com.example.Cambo_MarketPlace.Repository.ProductRepository;
import com.example.Cambo_MarketPlace.Repository.UserRepository;

@Service
public class ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Get or create a chat room
     */
    @Transactional
    public ChatRoom getOrCreateChatRoom(Long productId, Long buyerId, Long sellerId) {
        // Validation: Buyer and Seller cannot be the same person
        if (buyerId.equals(sellerId)) {
            throw new IllegalArgumentException("Buyer and seller cannot be the same person");
        }
        
        Optional<ChatRoom> existingRoom = chatRoomRepository
                .findByProductIdAndBuyerIdAndSellerId(productId, buyerId, sellerId);
        
        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }
        
        // Fetch entities
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<User> buyerOpt = userRepository.findById(buyerId);
        Optional<User> sellerOpt = userRepository.findById(sellerId);
        
        if (productOpt.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        if (buyerOpt.isEmpty()) {
            throw new RuntimeException("Buyer not found with id: " + buyerId);
        }
        if (sellerOpt.isEmpty()) {
            throw new RuntimeException("Seller not found with id: " + sellerId);
        }
        
        // Validation: Ensure the seller is the actual owner of the product
        Product product = productOpt.get();
        if (product.getSeller() != null && !product.getSeller().getId().equals(sellerId)) {
            throw new IllegalArgumentException("Seller ID does not match product owner");
        }
        
        // Create new chat room
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setProduct(productOpt.get());
        chatRoom.setBuyer(buyerOpt.get());
        chatRoom.setSeller(sellerOpt.get());
        chatRoom.setCreatedAt(LocalDateTime.now());
        
        return chatRoomRepository.save(chatRoom);
    }

    /**
     * Save a message to the database
     */
    @Transactional
    public Message saveMessage(ChatMessageDTO messageDTO) {
        Optional<ChatRoom> chatRoomOpt = chatRoomRepository.findById(messageDTO.getChatRoomId());
        Optional<User> senderOpt = userRepository.findById(messageDTO.getSenderId());
        
        if (chatRoomOpt.isEmpty() || senderOpt.isEmpty()) {
            throw new RuntimeException("ChatRoom or User not found");
        }
        
        Message message = new Message();
        message.setContent(messageDTO.getContent());
        message.setSender(senderOpt.get());
        message.setChatRoom(chatRoomOpt.get());
        message.setSentAt(LocalDateTime.now());
        
        return messageRepository.save(message);
    }

    /**
     * Get all messages in a chat room
     */
    public List<Message> getChatRoomMessages(Long chatRoomId) {
        return messageRepository.findByChatRoomIdOrderBySentAtAsc(chatRoomId);
    }

    /**
     * Get all chat rooms for a user
     */
    public List<ChatRoom> getUserChatRooms(Long userId) {
        return chatRoomRepository.findByUserId(userId);
    }

    /**
     * Get a specific chat room
     */
    public Optional<ChatRoom> getChatRoom(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId);
    }

    /**
     * Convert Message entity to DTO
     */
    public ChatMessageDTO convertToDTO(Message message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setChatRoomId(message.getChatRoom().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        dto.setType(ChatMessageDTO.MessageType.CHAT);
        return dto;
    }
}
