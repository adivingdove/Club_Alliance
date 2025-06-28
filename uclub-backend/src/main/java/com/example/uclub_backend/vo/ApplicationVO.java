package com.example.uclub_backend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationVO {
    private Integer id;
    private Integer clubId;
    private String clubName;
    private Integer userId;
    private String applicantName;
    private String applicantInfo;
    private String reason;
    private String status;
    private LocalDateTime appliedAt;
    private LocalDateTime processedAt;
    private Integer creatorId;
} 
