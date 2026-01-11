package com.example.Cambo_MarketPlace.Repository;

import com.example.Cambo_MarketPlace.Models.ChatRoom;
import com.example.Cambo_MarketPlace.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    // Find all chat rooms for a specific user (as buyer or seller)
    @Query("SELECT c FROM ChatRoom c WHERE c.buyer.id = :userId OR c.seller.id = :userId ORDER BY c.createdAt DESC")
    List<ChatRoom> findByUserId(Long userId);
    
    // Find chat room between buyer and seller for a specific product
    Optional<ChatRoom> findByProductIdAndBuyerIdAndSellerId(Long productId, Long buyerId, Long sellerId);
    
    // Find all chat rooms for a specific product
    List<ChatRoom> findByProductId(Long productId);
}
