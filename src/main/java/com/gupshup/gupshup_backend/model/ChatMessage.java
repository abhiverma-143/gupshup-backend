package com.gupshup.gupshup_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity // ✅ Ye batata hai ki ye Database Table hai
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Har message ka unique number
    
    private String content;
    private String sender;
    private String receiver; // Lombok apne aap iske Getters/Setters bana lega

    @Enumerated(EnumType.STRING)
    private MessageType type;

    private String timestamp; // Kab bheja gaya?

    // 👇 NAYI CHEEZ: Message ka status track karne ke liye
    private String status; 

    // Message bante hi time aur default status apne aap set ho jayega
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now().toString();
        
        // Agar status pehle se set nahi hai, to default "SENT" maan lo
        if (this.status == null) {
            this.status = "SENT"; 
        }
    }

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        TYPING,     // 👈 Typing indicator ke liye
        DELIVERED,  // 👈 Double tick ke liye
        SEEN        // 👈 Blue tick ke liye
    }
}