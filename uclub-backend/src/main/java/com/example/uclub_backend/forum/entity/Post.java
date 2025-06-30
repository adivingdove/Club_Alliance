package com.example.uclub_backend.forum.entity;
import jakarta.persistence.*;

import lombok.Data;

import java.util.List;

@Entity
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clubId;
    private Long userId;
    private String title;

    @Lob
    private String content;

    @Column(columnDefinition = "json")
    private String imageUrls; // JSON 字符串形式存储

    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.active;

    private Integer likeCount = 0;
    private Integer commentCount = 0;
    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @Transient
    private List<String> imageUrlList; // 用于接收前端数组，但不持久化

   @PostLoad
private void onLoad() {
    if (this.imageUrls != null) {
        try {
            this.imageUrlList = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(this.imageUrls, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception ignored) {}
    }
}


    @PrePersist
    @PreUpdate
    private void onSave() {
        // List -> JSON
        if (this.imageUrlList != null) {
            try {
                this.imageUrls = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(this.imageUrlList);
            } catch (Exception ignored) {}
        }
    }

    @Transient // 表示这个字段不映射数据库，只作为临时数据用
private String clubName;

public String getClubName() {
    return clubName;
}

public void setClubName(String clubName) {
    this.clubName = clubName;
}
}
