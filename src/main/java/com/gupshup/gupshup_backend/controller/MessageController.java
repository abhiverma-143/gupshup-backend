package com.gupshup.gupshup_backend.controller;

import com.gupshup.gupshup_backend.model.ChatMessage;
import com.gupshup.gupshup_backend.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*") // React se request allow karne ke liye
public class MessageController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // 👇 React is API ko call karke purani chat mangega
    @GetMapping("/{user1}/{user2}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String user1, @PathVariable String user2) {
        
        List<ChatMessage> history = chatMessageRepository.findChatHistory(user1, user2);
        
        return ResponseEntity.ok(history);
    }
}