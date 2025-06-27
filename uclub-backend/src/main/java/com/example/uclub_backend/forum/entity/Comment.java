package com.example.uclub_backend.forum.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自增主键（MySQL/Auto）
    private Long id;

    private Long postId;
    private Long userId;
    private String content;
    private CommentStatus status;

    @Column(name = "like_count")
    private Integer likeCount;

    private LocalDateTime createdAt;
}