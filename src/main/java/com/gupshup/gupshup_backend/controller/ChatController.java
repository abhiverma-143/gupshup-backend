package com.gupshup.gupshup_backend.controller;

import com.gupshup.gupshup_backend.model.ChatMessage;
import com.gupshup.gupshup_backend.repository.ChatMessageRepository;
import com.gupshup.gupshup_backend.service.GroqService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.List;

@Controller
@CrossOrigin(origins = "*") 
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ChatMessageRepository chatRepository;

    @Autowired
    private GroqService groqService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        if (chatMessage.getType() == ChatMessage.MessageType.CHAT) {
            chatRepository.save(chatMessage);
        }
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, 
                               SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

    @GetMapping("/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory() {
        return chatRepository.findAll();
    }

    @MessageMapping("/chat.typing")
    @SendTo("/topic/public")
    public ChatMessage typing(@Payload ChatMessage chatMessage) {
        chatMessage.setType(ChatMessage.MessageType.TYPING);
        return chatMessage;
    }

    // 🔥 UPDATED PRIVATE CHAT METHOD (Infinite Loop Fixed + History Added)
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage) {
        
        // 1. Agar asli CHAT message hai (Text/Image/Audio), tabhi database me save karo.
        // Taki 'SEEN' aur 'DELIVERED' status database ko na bharein.
        if (chatMessage.getType() == ChatMessage.MessageType.CHAT) {
            chatRepository.save(chatMessage);
        }

        // 2. Message ko samne wale receiver ko bhej do (Insaan ho ya AI)
        simpMessagingTemplate.convertAndSendToUser(
            chatMessage.getReceiver(), 
            "/private", 
            chatMessage
        );

        // 3. 👉 AI KA LOGIC: GupShup AI sirf tabhi reply kare jab usko msg mile aur msg type 'CHAT' ho!
        if ("GupShup AI".equals(chatMessage.getReceiver()) && chatMessage.getType() == ChatMessage.MessageType.CHAT) {
            
            // A. Call Groq API
            String aiResponse = groqService.getChatResponse(chatMessage.getContent());

            // B. Prepare AI Response
            ChatMessage response = new ChatMessage();
            response.setSender("GupShup AI");
            response.setReceiver(chatMessage.getSender());
            response.setContent(aiResponse);
            response.setType(ChatMessage.MessageType.CHAT);
            response.setStatus("SENT");
            response.setTimestamp(Instant.now().toString()); // Timestamp zaroori hai React ke liye

            // C. AI ka message bhi database me save karo (Taki history bani rahe)
            chatRepository.save(response);

            // D. Send back to User
            simpMessagingTemplate.convertAndSendToUser(
                chatMessage.getSender(), 
                "/private", 
                response
            );
        }
    }
}