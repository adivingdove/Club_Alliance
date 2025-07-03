package com.example.uclub_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "club_activity")
public class ClubActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "club_id", nullable = false)
    private Integer clubId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "max_participants")
    private Integer maxParticipants;
    
    @Column(name = "current_participants")
    private Integer currentParticipants = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "apply_status")
    private ApplyStatus applyStatus = ApplyStatus.待审核;
    
    @Column(name = "creator_id", nullable = false)
    private Integer creatorId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    public enum ApplyStatus {
        待审核, 通过, 拒绝
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 
