package com.example.uclub_backend.forum.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "post")
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 社团ID
    @Column(name = "club_id")
    @JsonProperty("club_id")
    private Long clubId;

    // 用户ID
    @Column(name = "user_id")
    @JsonProperty("user_id")
    private Long userId;

    private String title;
    private String content;

    // 图片URL列表
    @Column(name = "image_urls", columnDefinition = "TEXT")
    @Convert(converter = ListToJsonConverter.class)
    @JsonProperty("image_urls")
    private List<String> imageUrls;

    private String status = "active";

    //  点赞数
    @Column(name = "like_count")
    @JsonProperty("like_count")
    private Integer likeCount = 0;

    //  评论数
    @Column(name = "comment_count")
    @JsonProperty("comment_count")
    private Integer commentCount = 0;

    // 发布时间
    @Column(name = "created_at")
    @JsonProperty("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // 非数据库字段：社团名称（前端展示用）
    @Transient
    @JsonProperty("club_name")
    private String clubName;
}
