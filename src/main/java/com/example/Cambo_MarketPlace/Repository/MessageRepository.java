package com.example.Cambo_MarketPlace.Repository;

import com.example.Cambo_MarketPlace.Models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // Find all messages in a chat room, ordered by sent time
    List<Message> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);
    
    // Find recent messages in a chat room (useful for pagination)
    List<Message> findTop50ByChatRoomIdOrderBySentAtDesc(Long chatRoomId);
}
