package com.example.uclub_backend.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String creatorName;
}
