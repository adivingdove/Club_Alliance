package com.example.uclub_backend.forum.entity;

import lombok.Data;

import java.time.LocalDateTime;

import jakarta.persistence.Column;

@Data
public class Comment {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private CommentStatus status;
    @Column(name = "like_count")
    private Integer likeCount;
    private LocalDateTime createdAt;
}
