package com.example.Cambo_MarketPlace.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Cambo_MarketPlace.Models.ChatRoom;
import com.example.Cambo_MarketPlace.Repository.ChatRoomRepository;
import com.example.Cambo_MarketPlace.Repository.UserRepository;
import com.example.Cambo_MarketPlace.Service.ChatService;

@RestController
@RequestMapping("/api/chatrooms")
public class ChatRoomController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get or create a chat room for a product
     */
    @PostMapping("/create-or-get")
    public ResponseEntity<?> createOrGetChatRoom(@RequestBody Map<String, Long> request) {
        Long productId = request.get("productId");
        Long buyerId = request.get("buyerId");
        Long sellerId = request.get("sellerId");

        if (productId == null || buyerId == null || sellerId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "productId, buyerId, and sellerId are required"));
        }

        // Validation: Buyer and Seller cannot be the same person
        if (buyerId.equals(sellerId)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "You cannot chat with yourself"));
        }

        try {
            ChatRoom chatRoom = chatService.getOrCreateChatRoom(productId, buyerId, sellerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("chatRoomId", chatRoom.getId());
            response.put("buyerId", chatRoom.getBuyer().getId());
            response.put("buyerUsername", chatRoom.getBuyer().getUsername());
            response.put("sellerId", chatRoom.getSeller().getId());
            response.put("sellerUsername", chatRoom.getSeller().getUsername());
            if (chatRoom.getProduct() != null) {
                response.put("productId", chatRoom.getProduct().getId());
                response.put("productTitle", chatRoom.getProduct().getTitle());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get user's chat rooms
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserChatRooms(@PathVariable Long userId) {
        try {
            List<ChatRoom> chatRooms = chatService.getUserChatRooms(userId);
            
            // Convert to DTOs to avoid circular references
            List<Map<String, Object>> response = chatRooms.stream().map(room -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", room.getId());
                dto.put("chatRoomId", room.getId());
                
                // Buyer info
                Map<String, Object> buyer = new HashMap<>();
                buyer.put("id", room.getBuyer().getId());
                buyer.put("username", room.getBuyer().getUsername());
                dto.put("buyer", buyer);
                
                // Seller info
                Map<String, Object> seller = new HashMap<>();
                seller.put("id", room.getSeller().getId());
                seller.put("username", room.getSeller().getUsername());
                dto.put("seller", seller);
                
                // Product info
                if (room.getProduct() != null) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", room.getProduct().getId());
                    product.put("title", room.getProduct().getTitle());
                    dto.put("product", product);
                }
                
                return dto;
            }).toList();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get chat room details
     */
    @GetMapping("/{chatRoomId}")
    public ResponseEntity<?> getChatRoom(@PathVariable Long chatRoomId) {
        Optional<ChatRoom> chatRoom = chatService.getChatRoom(chatRoomId);
        
        if (chatRoom.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ChatRoom room = chatRoom.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", room.getId());
        response.put("chatRoomId", room.getId());
        
        // Buyer info
        Map<String, Object> buyer = new HashMap<>();
        buyer.put("id", room.getBuyer().getId());
        buyer.put("username", room.getBuyer().getUsername());
        response.put("buyer", buyer);
        response.put("buyerId", room.getBuyer().getId());
        response.put("buyerUsername", room.getBuyer().getUsername());
        
        // Seller info
        Map<String, Object> seller = new HashMap<>();
        seller.put("id", room.getSeller().getId());
        seller.put("username", room.getSeller().getUsername());
        response.put("seller", seller);
        response.put("sellerId", room.getSeller().getId());
        response.put("sellerUsername", room.getSeller().getUsername());
        
        // Product info
        if (room.getProduct() != null) {
            Map<String, Object> product = new HashMap<>();
            product.put("id", room.getProduct().getId());
            product.put("title", room.getProduct().getTitle());
            response.put("product", product);
            response.put("productId", room.getProduct().getId());
            response.put("productTitle", room.getProduct().getTitle());
        }
        
        return ResponseEntity.ok(response);
    }
}
