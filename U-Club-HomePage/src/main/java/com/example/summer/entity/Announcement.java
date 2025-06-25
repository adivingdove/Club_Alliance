package com.example.summer.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "announcement")
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "club_id")
    private Integer clubId; // 为NULL表示系统公告
    
    @Column(nullable = false)
    private String title;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AnnouncementType type;
    
    @Column(nullable = false)
    private String content;
    
    @Column(name = "creator_id", nullable = false)
    private Integer creatorId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum AnnouncementType {
        系统, 社团
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 