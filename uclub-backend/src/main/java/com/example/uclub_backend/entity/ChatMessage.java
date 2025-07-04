package com.example.uclub_backend.entity;
import lombok.Data;

@Data
public class ChatMessage {
    private String sender; 
    private String avatar; 
    private String content;
    private String time;
    private String role; 
    private String room;
    // Getters and Setters
}
