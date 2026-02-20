package com.gupshup.gupshup_backend.repository;

import com.gupshup.gupshup_backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 👇 NEW: Do logon (User1 aur User2) ke beech ki saari chat nikalne ki query
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND m.receiver = :user1) ORDER BY m.id ASC")
    List<ChatMessage> findChatHistory(@Param("user1") String user1, @Param("user2") String user2);
    
}