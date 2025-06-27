package com.example.uclub_backend.forum.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private CommentStatus status;
    private Integer likeCount;
    private LocalDateTime createdAt;
}
