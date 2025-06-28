package com.example.uclub_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "activity_participant")
public class ActivityParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "activity_id", nullable = false)
    private Integer activityId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "join_time")
    private LocalDateTime joinTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ParticipantStatus status = ParticipantStatus.已加入;
    
    public enum ParticipantStatus {
        已加入, 已退出
    }
    
    @PrePersist
    protected void onCreate() {
        joinTime = LocalDateTime.now();
    }
} 