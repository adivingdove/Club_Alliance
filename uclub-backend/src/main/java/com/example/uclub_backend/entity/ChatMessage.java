package com.example.uclub_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String room;

    private String sender;

    private String avatar;

    @Lob
    private String content;

    private String role;

    private LocalDateTime time;

    @Column(name = "sender_id")
    private Integer senderId; 
}
