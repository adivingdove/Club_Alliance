package com.example.uclub_backend.admin.entity;

import com.example.uclub_backend.club.entity.Club;
import com.example.uclub_backend.model.User;
import jakarta.persistence.*;

import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcement")
@Data
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    private String title;

    @Enumerated(EnumType.STRING)
    private Type type; // 系统 or 社团

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    private LocalDateTime createdAt;

    public enum Type {
        系统, 社团
    }
}

