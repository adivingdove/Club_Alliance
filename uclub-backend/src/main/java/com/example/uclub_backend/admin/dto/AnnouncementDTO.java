// AnnouncementDTO.java
package com.example.uclub_backend.admin.dto;

import java.time.LocalDateTime;

public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String creatorName;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
}
