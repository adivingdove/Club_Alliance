package com.example.uclub_backend.forum.entity;
import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;




@Entity
@Table(name = "post")
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

    @JsonProperty("image_urls")
    @Column(columnDefinition = "TEXT")
    private String image_urls;

    private String status="active";;
    
    @JsonProperty("like_count")
    private Integer like_count=0;

    @JsonProperty("comment_count")
    private Integer comment_count=0;

    @JsonProperty("created_at")
    private String created_at=LocalDateTime.now().toString();
};



