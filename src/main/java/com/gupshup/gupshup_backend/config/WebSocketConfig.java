package com.gupshup.gupshup_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. Connection Point: Yahan Frontend connect karega
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Filhal sabko allow kar rahe hain
                .withSockJS(); // Agar WebSocket fail ho, to backup use karo
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. Message Broker (Postman):
        // "/topic" wahi messages hain jo sabko broadcasting honge (Group Chat)
        registry.enableSimpleBroker("/topic");
        
        // "/app" wo messages hain jo Client -> Server bheje jayenge
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/user"); // 👈 "/user" add karein
        registry.setUserDestinationPrefix("/user");
    }
}

