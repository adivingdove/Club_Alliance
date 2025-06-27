package com.example.uclub_backend.forum.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("club_id")
    private Long club_id;

    @JsonProperty("user_id")
    private Long user_id;

    private String title;
    private String content;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    @Convert(converter = ListToJsonConverter.class)  //  添加转换器
    @JsonProperty("image_urls")
    private List<String> imageUrls;

    private String status = "active";

    @JsonProperty("like_count")
    private Integer like_count = 0;

    @JsonProperty("comment_count")
    private Integer comment_count = 0;

    @JsonProperty("created_at")
    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Transient
   @JsonProperty("club_name")
    private String clubName;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
    }

    public Integer getLikeCount() {
        return like_count;
    }

    public void setLikeCount(Integer likeCount) {
        this.like_count = likeCount;
    }
}
